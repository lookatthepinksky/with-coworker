import { useState, useEffect } from 'react'
import DashboardHeader from '@pages/dashboard/components/DashboardHeader.jsx'
import '@styles/result.css'

const tabs = [
  { key: '1m', label: '이번달' },
  { key: '3m', label: '3개월' },
  { key: '6m', label: '6개월' },
  { key: '1y', label: '1년' },
]

const allComments = [
  { text: '같이 일하기 정말 편한 동료예요. 항상 먼저 도와주려고 해서 고마워요 😊', date: '2026.03' },
  { text: '실행력이 빠르고 문제가 생겼을 때 당황하지 않고 잘 해결해요.', date: '2026.03' },
  { text: '커뮤니케이션을 좀 더 자주 해줬으면 좋겠어요. 진행 상황 공유가 가끔 늦어요.', date: '2026.03' },
  { text: '지식 공유를 더 자주 해줬으면 해요. 혼자 알고 있는 게 많은 것 같아요.', date: '2026.02' },
  { text: '눈에 띄게 성장하고 있어요. 계속 이렇게만 해줘요!', date: '2026.02' },
  { text: '꼼꼼하게 작업하는 모습이 인상적이에요.', date: '2026.01' },
  { text: '마감 전날 항상 바빠 보이는데 일정 조율을 좀 더 하면 좋을 것 같아요.', date: '2025.12' },
  { text: '어려운 문제도 포기하지 않고 끝까지 해결하는 모습이 멋있어요.', date: '2025.11' },
  { text: '팀 분위기를 밝게 만들어줘서 고마워요 😄', date: '2025.10' },
  { text: '처음엔 소통이 좀 어색했는데 많이 나아진 것 같아요.', date: '2025.07' },
  { text: '코드 리뷰 피드백을 잘 반영해줘서 같이 일하기 편해요.', date: '2025.06' },
  { text: '성장 속도가 빠른 팀원이에요. 앞으로도 기대돼요 🚀', date: '2025.04' },
]

const periodData = {
  '1m': {
    period: '2026년 3월',
    commentMonths: ['2026.03'],
    scores: [
      { label: '🗣️ 의사소통', current: 4.2, prev: 3.8 },
      { label: '📚 지식공유', current: 3.8, prev: 4.0 },
      { label: '🙋 적극성', current: 4.5, prev: 4.1 },
      { label: '🔧 문제해결', current: 4.0, prev: 3.5 },
      { label: '⏱️ 일정 준수', current: 3.6, prev: 3.6 },
      { label: '🎯 정확성', current: 4.3, prev: 4.0 },
    ],
  },
  '3m': {
    period: '2026년 1월 ~ 3월',
    commentMonths: ['2026.01', '2026.02', '2026.03'],
    scores: [
      { label: '🗣️ 의사소통', current: 4.0, prev: 3.6 },
      { label: '📚 지식공유', current: 3.6, prev: 3.4 },
      { label: '🙋 적극성', current: 4.3, prev: 4.0 },
      { label: '🔧 문제해결', current: 3.8, prev: 3.5 },
      { label: '⏱️ 일정 준수', current: 3.5, prev: 3.3 },
      { label: '🎯 정확성', current: 4.1, prev: 3.8 },
    ],
  },
  '6m': {
    period: '2025년 10월 ~ 2026년 3월',
    commentMonths: ['2025.10', '2025.11', '2025.12', '2026.01', '2026.02', '2026.03'],
    scores: [
      { label: '🗣️ 의사소통', current: 3.8, prev: 3.3 },
      { label: '📚 지식공유', current: 3.5, prev: 3.1 },
      { label: '🙋 적극성', current: 4.1, prev: 3.7 },
      { label: '🔧 문제해결', current: 3.7, prev: 3.3 },
      { label: '⏱️ 일정 준수', current: 3.4, prev: 3.0 },
      { label: '🎯 정확성', current: 3.9, prev: 3.5 },
    ],
  },
  '1y': {
    period: '2025년 4월 ~ 2026년 3월',
    commentMonths: ['2025.04', '2025.05', '2025.06', '2025.07', '2025.08', '2025.09', '2025.10', '2025.11', '2025.12', '2026.01', '2026.02', '2026.03'],
    scores: [
      { label: '🗣️ 의사소통', current: 3.6, prev: 2.9 },
      { label: '📚 지식공유', current: 3.3, prev: 2.8 },
      { label: '🙋 적극성', current: 3.9, prev: 3.2 },
      { label: '🔧 문제해결', current: 3.5, prev: 2.9 },
      { label: '⏱️ 일정 준수', current: 3.2, prev: 2.7 },
      { label: '🎯 정확성', current: 3.7, prev: 3.0 },
    ],
  },
}

function Result() {
  const [activeTab, setActiveTab] = useState('1m')
  const [animated, setAnimated] = useState(false)
  const [openMonths, setOpenMonths] = useState({})

  const data = periodData[activeTab]
  const comments = allComments.filter((c) => data.commentMonths.includes(c.date))

  const commentsByMonth = comments.reduce((acc, c) => {
    if (!acc[c.date]) acc[c.date] = []
    acc[c.date].push(c)
    return acc
  }, {})
  const sortedMonths = Object.keys(commentsByMonth).sort((a, b) => b.localeCompare(a))

  const toggleMonth = (month) => {
    setOpenMonths((prev) => ({ ...prev, [month]: !prev[month] }))
  }

  const average = (data.scores.reduce((sum, s) => sum + s.current, 0) / data.scores.length).toFixed(1)
  const best = data.scores.reduce((a, b) => a.current > b.current ? a : b)
  const worst = data.scores.reduce((a, b) => a.current < b.current ? a : b)

  useEffect(() => {
    setAnimated(false)
    setOpenMonths({})
    const timer = setTimeout(() => setAnimated(true), 100)
    return () => clearTimeout(timer)
  }, [activeTab])

  return (
    <>
      <DashboardHeader />
      <main className="result-main">

        <div className="result-top">
          <a href="/dashboard" className="btn-back">← 대시보드로</a>
          <h1 className="result-title">내 평가 결과</h1>
          <p className="result-month">{data.period} 기준</p>
        </div>

        <div className="result-tabs">
          {tabs.map((tab) => (
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
            <span className="summary-score">{average}<span className="summary-max"> / 5.0</span></span>
          </div>
          <div className="summary-card">
            <span className="summary-label">평가한 팀원 수</span>
            <span className="summary-score">5<span className="summary-max">명</span></span>
          </div>
          <div className="summary-card">
            <span className="summary-label">가장 높은 항목</span>
            <span className="summary-best">{best.label}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">가장 낮은 항목</span>
            <span className="summary-worst">{worst.label}</span>
          </div>
        </div>

        <div className="result-content">
          <section className="result-section">
            <h2 className="section-label">항목별 점수</h2>
            <p className="section-sub-desc">현재 vs 이전 기간 비교예요</p>
            <div className="score-list">
              {data.scores.map((s) => {
                const diff = (s.current - s.prev).toFixed(1)
                const isUp = diff > 0
                const isSame = diff == 0
                return (
                  <div className="score-item" key={s.label}>
                    <div className="score-top">
                      <span className="score-label">{s.label}</span>
                      <div className="score-right">
                        <span className={`score-diff ${isUp ? 'up' : isSame ? 'same' : 'down'}`}>
                          {isSame ? '→' : isUp ? `▲ +${diff}` : `▼ ${diff}`}
                        </span>
                        <span className="score-value">{s.current}</span>
                      </div>
                    </div>
                    <div className="bar-wrap">
                      <div className="bar-bg">
                        <div className="bar-fill" style={{ width: animated ? `${(s.current / 5) * 100}%` : '0%' }} />
                      </div>
                      <div className="bar-bg prev">
                        <div className="bar-fill prev" style={{ width: animated ? `${(s.prev / 5) * 100}%` : '0%' }} />
                      </div>
                    </div>
                    <div className="bar-legend">
                      <span className="legend-current">● 현재 {s.current}</span>
                      <span className="legend-prev">● 이전 {s.prev}</span>
                    </div>
                  </div>
                )
              })}
            </div>
          </section>

          <section className="result-section">
            <h2 className="section-label">팀원 코멘트</h2>
            <p className="section-sub-desc">월별로 펼쳐서 볼 수 있어요</p>
            <div className="comment-accordion">
              {sortedMonths.map((month) => (
                <div className="accordion-item" key={month}>
                  <button className="accordion-header" onClick={() => toggleMonth(month)}>
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
          </section>
        </div>

      </main>
    </>
  )
}

export default Result
