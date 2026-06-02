import { useState, useEffect, useRef } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis
} from 'recharts'
import { pdf } from '@react-pdf/renderer'
import html2canvas from 'html2canvas'
import ResultPdfDocument from './ResultPdfDocument'
import DashboardHeader from '@pages/dashboard/components/DashboardHeader.jsx'
import api from '@api/client'
import '@styles/result.css'

const TABS = [
  { key: '1m', label: '최근 1개월' },
  { key: '3m', label: '최근 3개월' },
  { key: '6m', label: '최근 6개월' },
  { key: '1y', label: '최근 1년' },
]

const TAB_MONTHS = { '1m': 1, '3m': 3, '6m': 6, '1y': 12 }

const TREND_COLORS = ['#7C6FF7', '#34D399', '#F59E0B', '#60A5FA', '#F87171', '#A78BFA']

function Result() {
  const [activeTab, setActiveTab] = useState('1m')
  const [animated, setAnimated] = useState(false)
  const [openMonths, setOpenMonths] = useState({})
  const [summaryData, setSummaryData] = useState(null)
  const [summaryLoading, setSummaryLoading] = useState(true)
  const [trendData, setTrendData] = useState([])
  const [latestEvaluatorCount, setLatestEvaluatorCount] = useState(null)
  const [trendLoading, setTrendLoading] = useState(true)
  const [pdfLoading, setPdfLoading] = useState(false)
  const [chartReady, setChartReady] = useState(false)
  const radarChartRef = useRef(null)
  const trendChartRef = useRef(null)

  useEffect(() => {
    if (trendLoading || summaryLoading) {
      setChartReady(false)
    } else {
      const timer = setTimeout(() => setChartReady(true), 500)
      return () => clearTimeout(timer)
    }
  }, [trendLoading, summaryLoading])

  useEffect(() => {
    api.get('/api/result/latest-evaluator-count')
      .then(({ data }) => setLatestEvaluatorCount(data.count))
  }, [])

  useEffect(() => {
    setSummaryLoading(true)
    setTrendLoading(true)
    setAnimated(false)
    setOpenMonths({})
    api.get(`/api/result/summary?months=${TAB_MONTHS[activeTab]}`)
      .then(({ data }) => {
        setSummaryData(data)
        setTimeout(() => setAnimated(true), 100)
      })
      .finally(() => setSummaryLoading(false))
    api.get(`/api/result/trend?months=${TAB_MONTHS[activeTab]}`)
      .then(({ data }) => setTrendData(data.data ?? []))
      .finally(() => setTrendLoading(false))
  }, [activeTab])

  const chartData = trendData.map(point => {
    const obj = { month: point.month }
    point.scores.forEach(s => { obj[s.label] = s.score })
    return obj
  })
  const itemLabels = trendData[0]?.scores.map(s => s.label) ?? []

  const oneMonthData = itemLabels.map(label => {
    const row = { label }
    row[label] = trendData[0]?.scores.find(s => s.label === label)?.score
    return row
  })

  const scores = summaryData?.scores ?? []
  const average = scores.length > 0
    ? (scores.reduce((sum, s) => sum + s.current, 0) / scores.length).toFixed(1)
    : null
  const best = scores.length > 0 ? scores.reduce((a, b) => a.current > b.current ? a : b) : null
  const worst = scores.length > 0 ? scores.reduce((a, b) => a.current < b.current ? a : b) : null

  const commentsByMonth = (summaryData?.comments ?? []).reduce((acc, c) => {
    if (!acc[c.month]) acc[c.month] = []
    acc[c.month].push(c)
    return acc
  }, {})
  const sortedMonths = Object.keys(commentsByMonth).sort((a, b) => b.localeCompare(a))

  const exportPdf = async () => {
    setPdfLoading(true)
    try {
      // 차트 이미지 캡처
      const captureChart = async (ref) => {
        if (!ref.current) return null
        const canvas = await html2canvas(ref.current, {
          scale: 2, backgroundColor: '#ffffff', logging: false,
        })
        return canvas.toDataURL('image/png')
      }

      const [trendChartImageUrl, radarChartImageUrl] = await Promise.all([
        captureChart(trendChartRef),
        captureChart(radarChartRef),
      ])

      const today = new Date()
      const printDate = `${today.getFullYear()}년 ${today.getMonth() + 1}월 ${today.getDate()}일`

      const blob = await pdf(
        <ResultPdfDocument
          summaryData={summaryData}
          scores={scores}
          sortedMonths={sortedMonths}
          commentsByMonth={commentsByMonth}
          printDate={printDate}
          trendChartImageUrl={trendChartImageUrl}
          radarChartImageUrl={radarChartImageUrl}
          trendData={trendData}
        />
      ).toBlob()

      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      const period = (summaryData?.period ?? '').replace(/\s+/g, '').replace('기준', '')
      a.href = url
      a.download = `평가결과_${period || '결과'}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } finally {
      setPdfLoading(false)
    }
  }

  return (
    <>
      {pdfLoading && (
        <div className="pdf-loading-overlay">
          <div className="pdf-loading-spinner" />
        </div>
      )}
      <DashboardHeader />
      <main className="result-main">

        <div className="page-nav">
          <a href="/dashboard" className="page-nav-btn">팀원 평가</a>
          <a href="/result" className="page-nav-btn active">내 평가</a>
        </div>

        <div className="result-top">
          <div>
            <h1 className="result-title">내 평가 결과</h1>
            <p className="result-month">{summaryData?.period ?? ''} 기준</p>
          </div>
          <button
            className="pdf-export-btn"
            onClick={exportPdf}
            disabled={pdfLoading || summaryLoading || trendLoading || !chartReady}
          >
            PDF 내보내기
          </button>
        </div>

        <div className="result-tabs">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              className={`result-tab ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="result-summary">
          <div className="summary-card">
            <span className="summary-label">종합 평균</span>
            <span className="summary-score">
              {average ?? '-'}<span className="summary-max"> / 5.0</span>
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">최근 평가 팀원 수</span>
            <span className="summary-score">
              {latestEvaluatorCount ?? '-'}<span className="summary-max">명</span>
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">가장 높은 항목</span>
            <span className="summary-best">{best?.label ?? '-'}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">가장 낮은 항목</span>
            <span className="summary-worst">{worst?.label ?? '-'}</span>
          </div>
        </div>

        <section className="result-section chart-section" ref={trendChartRef}>
          <div className="chart-header">
            <div>
              <h2 className="result-section-label">점수 변화 추이</h2>
              <p className="section-sub-desc">
                {activeTab === '1m' ? '항목별 점수예요' : '항목별 월평균 점수예요'}
              </p>
            </div>
          </div>

          {trendLoading ? (
            <div className="chart-empty">불러오는 중...</div>
          ) : activeTab === '1m' ? (
            (trendData[0]?.scores ?? []).length === 0 ? (
              <div className="chart-empty">해당 기간에 평가 데이터가 없어요</div>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={oneMonthData} margin={{ top: 8, right: 16, bottom: 0, left: -16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" />
                  <XAxis dataKey="label" tick={{ fontSize: 12, fill: '#9CA3AF' }} padding={{ left: 60, right: 60 }} />
                  <YAxis domain={[1, 5]} ticks={[1, 2, 3, 4, 5]} tick={{ fontSize: 12, fill: '#9CA3AF' }} />
                  <Tooltip
                    contentStyle={{ border: '1.5px solid #E5E7EB', borderRadius: 10, fontSize: 13 }}
                    formatter={(value) => [value, '점수']}
                    cursor={false}
                  />
                  {itemLabels.map((label, i) => (
                    <Line
                      key={label}
                      dataKey={label}
                      stroke={TREND_COLORS[i % TREND_COLORS.length]}
                      strokeWidth={2}
                      dot={{ r: 5, strokeWidth: 2 }}
                      activeDot={{ r: 7 }}
                      animationDuration={400}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            )
          ) : chartData.length === 0 ? (
            <div className="chart-empty">해당 기간에 평가 데이터가 없어요</div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData} margin={{ top: 8, right: 16, bottom: 0, left: -16 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" />
                <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#9CA3AF' }} />
                <YAxis domain={[1, 5]} ticks={[1, 2, 3, 4, 5]} tick={{ fontSize: 12, fill: '#9CA3AF' }} />
                <Tooltip contentStyle={{ border: '1.5px solid #E5E7EB', borderRadius: 10, fontSize: 13 }} />
                <Legend wrapperStyle={{ fontSize: 13, paddingTop: 12 }} />
                {itemLabels.map((label, i) => (
                  <Line
                    key={label}
                    type="monotone"
                    dataKey={label}
                    stroke={TREND_COLORS[i % TREND_COLORS.length]}
                    strokeWidth={2}
                    dot={{ r: 5, strokeWidth: 2 }}
                    animationDuration={400}
                    activeDot={{ r: 7 }}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          )}
        </section>

        <div className="result-content">
          <section className="result-section">
            <h2 className="result-section-label">항목별 점수</h2>
            <p className="section-sub-desc">현재 vs 이전 기간 비교에요</p>
            {summaryLoading ? (
              <div className="no-result">불러오는 중...</div>
            ) : scores.length === 0 ? (
              <div className="no-result">해당 기간에 평가 데이터가 없어요</div>
            ) : (
              <div ref={radarChartRef}>
                <ResponsiveContainer width="100%" height={320}>
                  <RadarChart data={scores.map(s => ({ label: s.label, current: s.current, prev: s.prev }))}>
                    <PolarGrid stroke="#E5E7EB" />
                    <PolarAngleAxis dataKey="label" tick={{ fontSize: 12, fill: '#6B7280' }} />
                    <PolarRadiusAxis domain={[0, 5]} tickCount={6} tick={{ fontSize: 10, fill: '#9CA3AF' }} />
                    <Radar name="이전" dataKey="prev" stroke="#9CA3AF" fill="#9CA3AF" fillOpacity={0.2} strokeWidth={2} isAnimationActive={false} />
                    <Radar name="현재" dataKey="current" stroke="#6D28D9" fill="#6D28D9" fillOpacity={0.3} strokeWidth={2} isAnimationActive={false} />
                    <Legend
                      wrapperStyle={{ paddingTop: 60, paddingLeft: 20 }}
                      formatter={(value) => (
                        <span style={{ fontSize: 13, color: value === '현재' ? '#6D28D9' : '#9CA3AF' }}>{value}</span>
                      )}
                    />
                    <Tooltip
                      contentStyle={{ border: '1.5px solid #E5E7EB', borderRadius: 10, fontSize: 13 }}
                      formatter={(value, name) => [value, name]}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            )}
          </section>

          <section className="result-section">
            <h2 className="result-section-label">팀원 코멘트</h2>
            <p className="section-sub-desc">월별로 펼쳐서 볼 수 있어요</p>
            {summaryLoading ? (
              <div className="no-result">불러오는 중...</div>
            ) : sortedMonths.length === 0 ? (
              <div className="no-result">해당 기간에 코멘트가 없어요</div>
            ) : (
              <div className="comment-accordion">
                {sortedMonths.map((month) => (
                  <div className="accordion-item" key={month}>
                    <button
                      className="accordion-header"
                      onClick={() => setOpenMonths(prev => ({ ...prev, [month]: !prev[month] }))}
                    >
                      <span>{month}</span>
                      <span className="accordion-meta">
                        {commentsByMonth[month].length}개
                        <span className="accordion-arrow">{openMonths[month] ? '▲' : '▼'}</span>
                      </span>
                    </button>
                    {openMonths[month] && (
                      <div className="accordion-body">
                        {commentsByMonth[month].map((c, i) => (
                          <div className="comment-card" key={i}>
                            <p className="comment-text">"{c.text}"</p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

      </main>
    </>
  )
}

export default Result
