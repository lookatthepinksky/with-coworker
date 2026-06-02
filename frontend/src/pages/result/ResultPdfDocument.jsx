import { Document, Page, View, Text, Image, Font, StyleSheet } from '@react-pdf/renderer'
import fontRegular from '@assets/fonts/NanumGothic.ttf'
import fontBold from '@assets/fonts/NanumGothicBold.ttf'

Font.register({
  family: 'NanumGothic',
  fonts: [
    { src: fontRegular, fontWeight: 400 },
    { src: fontBold, fontWeight: 700 },
  ],
})

const strip = (str) =>
  (str ?? '').replace(/[^ -~가-힣ᄀ-ᇿ㄰-㆏0-9]/g, ' ').replace(/\s+/g, ' ').trim()

const c = {
  purple: '#6D28D9',
  border: '#E5E7EB',
  text: '#1F2937',
  textSub: '#6B7280',
  grayLight: '#F3F4F6',
  green: '#059669',
  red: '#DC2626',
  white: '#FFFFFF',
}

const s = StyleSheet.create({
  page: {
    fontFamily: 'NanumGothic',
    fontSize: 8,
    color: c.text,
    backgroundColor: c.white,
    paddingTop: 36,
    paddingBottom: 40,
    paddingHorizontal: 40,
  },

  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    marginBottom: 6,
  },
  title: { fontSize: 15, fontWeight: 700, color: c.text },
  period: {
    fontSize: 10,
    fontWeight: 700,
    color: c.purple,
    marginTop: 5,
    paddingVertical: 3,
    paddingHorizontal: 8,
    backgroundColor: '#EDE9FE',
    borderRadius: 3,
    alignSelf: 'flex-start',
  },
  printDate: { fontSize: 7, color: c.textSub },
  divider: { height: 2, backgroundColor: c.purple, borderRadius: 2, marginBottom: 18 },

  section: { marginBottom: 14 },
  sectionTitle: {
    fontSize: 9, fontWeight: 700, color: c.purple,
    paddingBottom: 4, borderBottomWidth: 1, borderBottomColor: c.border,
    marginBottom: 8,
  },

  summaryGrid: { flexDirection: 'row', gap: 6 },
  summaryCard: {
    flex: 1, backgroundColor: c.grayLight,
    borderWidth: 1, borderColor: c.border, borderRadius: 5,
    padding: 9,
  },
  summaryLabel: { fontSize: 7, color: c.textSub, marginBottom: 4 },
  summaryValue: { fontSize: 11, fontWeight: 700, color: c.text },
  summaryValueHigh: { fontSize: 11, fontWeight: 700, color: c.green },
  summaryValueLow: { fontSize: 11, fontWeight: 700, color: c.red },

  chartImage: { width: '100%', borderRadius: 4 },
  radarChartImage: { width: '55%', alignSelf: 'center', borderRadius: 4 },

  table: { borderWidth: 1, borderColor: c.border, borderRadius: 3, overflow: 'hidden' },
  tableHead: { flexDirection: 'row', backgroundColor: c.purple },
  tableHeadCell: {
    flex: 1, padding: '5 8', fontSize: 7,
    fontWeight: 700, color: c.white,
  },
  tableRow: { flexDirection: 'row' },
  tableRowAlt: { flexDirection: 'row', backgroundColor: c.grayLight },
  tableCell: {
    flex: 1, padding: '5 8', fontSize: 8, color: c.text,
    borderBottomWidth: 1, borderBottomColor: c.border,
  },
  diffUp: { color: c.green, fontWeight: 700 },
  diffDown: { color: c.red, fontWeight: 700 },
  diffSame: { color: c.textSub },

  // 월별 점수 테이블
  trendTable: { borderWidth: 1, borderColor: c.border, borderRadius: 3, overflow: 'hidden', marginTop: 8 },
  trendHead: { flexDirection: 'row', backgroundColor: '#EDE9FE' },
  trendHeadLabel: {
    width: 80, padding: '5 8', fontSize: 7,
    fontWeight: 700, color: c.purple,
    borderRightWidth: 1, borderRightColor: c.border,
  },
  trendHeadCell: {
    flex: 1, padding: '5 6', fontSize: 7,
    fontWeight: 700, color: c.purple, textAlign: 'center',
  },
  trendRow: { flexDirection: 'row' },
  trendRowAlt: { flexDirection: 'row', backgroundColor: '#F9FAFB' },
  trendLabelCell: {
    width: 80, padding: '5 8', fontSize: 7.5, color: c.text,
    borderRightWidth: 1, borderRightColor: c.border,
    borderBottomWidth: 1, borderBottomColor: c.border,
    fontWeight: 700,
  },
  trendDataCell: {
    flex: 1, padding: '5 6', fontSize: 7.5, color: c.text,
    textAlign: 'center',
    borderBottomWidth: 1, borderBottomColor: c.border,
  },

  monthLabel: {
    fontSize: 8, fontWeight: 700, color: c.text,
    backgroundColor: c.grayLight, padding: '4 7',
    borderRadius: 2, marginBottom: 4, marginTop: 8,
  },
  comment: {
    fontSize: 8, color: '#4B5563', lineHeight: 1.5,
    padding: '5 8', marginBottom: 3,
    borderLeftWidth: 2, borderLeftColor: '#C4B5FD',
    backgroundColor: '#FAFAFA',
  },

  footer: {
    position: 'absolute', bottom: 22, left: 40, right: 40,
    borderTopWidth: 1, borderTopColor: c.border,
    paddingTop: 7, textAlign: 'center',
    fontSize: 7, color: c.textSub,
  },
})

function ScoreDiffText({ diff }) {
  const isUp = diff > 0
  const isSame = diff === 0
  const label = isSame ? '-' : isUp ? `+${diff}` : `${diff}`
  const style = isSame ? s.diffSame : isUp ? s.diffUp : s.diffDown
  return <Text style={[s.tableCell, style]}>{label}</Text>
}

export default function ResultPdfDocument({
  summaryData, scores, sortedMonths, commentsByMonth,
  printDate, trendChartImageUrl, radarChartImageUrl, trendData,
}) {
  // 월별 테이블용 데이터 가공
  const trendMonths = (trendData ?? []).map(d => d.month)
  const trendItemLabels = trendData?.[0]?.scores.map(s => s.label) ?? []
  const trendScoreMap = {}
  ;(trendData ?? []).forEach(d => {
    trendScoreMap[d.month] = {}
    d.scores.forEach(s => { trendScoreMap[d.month][s.label] = s.score })
  })
  const average = scores.length > 0
    ? (scores.reduce((sum, s) => sum + s.current, 0) / scores.length).toFixed(1)
    : '-'
  const best = scores.length > 0 ? scores.reduce((a, b) => a.current > b.current ? a : b) : null
  const worst = scores.length > 0 ? scores.reduce((a, b) => a.current < b.current ? a : b) : null

  return (
    <Document>
      <Page size="A4" style={s.page}>

        <View style={s.header}>
          <View>
            <Text style={s.title}>동료평가 결과 보고서</Text>
            <Text style={s.period}>{strip(summaryData?.period ?? '')} 기준</Text>
          </View>
          <Text style={s.printDate}>출력일: {printDate}</Text>
        </View>
        <View style={s.divider} />

        {/* 평가 요약 */}
        <View style={s.section}>
          <Text style={s.sectionTitle}>평가 요약</Text>
          <View style={s.summaryGrid}>
            <View style={s.summaryCard}>
              <Text style={s.summaryLabel}>종합 평균</Text>
              <Text style={s.summaryValue}>{average} / 5.0</Text>
            </View>
            <View style={s.summaryCard}>
              <Text style={s.summaryLabel}>평가한 팀원 수</Text>
              <Text style={s.summaryValue}>{summaryData?.evaluatorCount ?? '-'}명</Text>
            </View>
            <View style={s.summaryCard}>
              <Text style={s.summaryLabel}>가장 높은 항목</Text>
              <Text style={s.summaryValueHigh}>{strip(best?.label ?? '-')}</Text>
            </View>
            <View style={s.summaryCard}>
              <Text style={s.summaryLabel}>가장 낮은 항목</Text>
              <Text style={s.summaryValueLow}>{strip(worst?.label ?? '-')}</Text>
            </View>
          </View>
        </View>

        {/* 점수 변화 추이 */}
        {trendChartImageUrl && (
          <View style={s.section} wrap={false}>
            <Text style={s.sectionTitle}>점수 변화 추이</Text>
            <Image src={trendChartImageUrl} style={s.chartImage} />

            {/* 월별 항목 점수 테이블 */}
            {trendMonths.length > 0 && trendItemLabels.length > 0 && (
              <View style={s.trendTable}>
                {/* 헤더 행 */}
                <View style={s.trendHead}>
                  <Text style={s.trendHeadLabel}>항목</Text>
                  {trendMonths.map(month => (
                    <Text key={month} style={s.trendHeadCell}>{month}</Text>
                  ))}
                </View>
                {/* 데이터 행 */}
                {trendItemLabels.map((label, i) => (
                  <View key={label} style={i % 2 === 0 ? s.trendRow : s.trendRowAlt}>
                    <Text style={s.trendLabelCell}>{strip(label)}</Text>
                    {trendMonths.map(month => (
                      <Text key={month} style={s.trendDataCell}>
                        {trendScoreMap[month]?.[label] != null
                          ? Number(trendScoreMap[month][label]).toFixed(1)
                          : '-'}
                      </Text>
                    ))}
                  </View>
                ))}
              </View>
            )}
          </View>
        )}

        {/* 항목별 점수 레이더 차트 */}
        {/* 항목별 점수 (레이더차트 + 상세 테이블 묶음) */}
        <View style={s.section} wrap={false}>
          {radarChartImageUrl && (
            <>
              <Text style={s.sectionTitle}>항목별 점수</Text>
              <View style={{ width: '65%', alignSelf: 'center' }}>
                <Image src={radarChartImageUrl} style={{ width: '100%', borderRadius: 4 }} />
              </View>
            </>
          )}

          {scores.length > 0 && (
            <View style={{ marginTop: radarChartImageUrl ? 10 : 0 }}>
              <Text style={s.sectionTitle}>항목별 점수 상세</Text>
              <View style={s.table}>
                <View style={s.tableHead}>
                  <Text style={s.tableHeadCell}>평가 항목</Text>
                  <Text style={s.tableHeadCell}>현재</Text>
                  <Text style={s.tableHeadCell}>이전</Text>
                  <Text style={s.tableHeadCell}>변화</Text>
                </View>
                {scores.map((item, i) => {
                  const diff = parseFloat((item.current - item.prev).toFixed(1))
                  return (
                    <View key={item.label} style={i % 2 === 0 ? s.tableRow : s.tableRowAlt}>
                      <Text style={s.tableCell}>{strip(item.label)}</Text>
                      <Text style={[s.tableCell, { fontWeight: 700 }]}>{item.current}</Text>
                      <Text style={s.tableCell}>{item.prev}</Text>
                      <ScoreDiffText diff={diff} />
                    </View>
                  )
                })}
              </View>
            </View>
          )}
        </View>

        {/* 팀원 코멘트 */}
        {sortedMonths.length > 0 && (
          <View style={s.section}>
            <Text style={s.sectionTitle}>팀원 코멘트</Text>
            {sortedMonths.map(month => (
              <View key={month}>
                <Text style={s.monthLabel}>{strip(month)}</Text>
                {commentsByMonth[month].map((c, i) => (
                  <Text key={i} style={s.comment}>{strip(c.text)}</Text>
                ))}
              </View>
            ))}
          </View>
        )}

        <Text style={s.footer}>본 문서는 시스템에서 자동 생성되었습니다</Text>

      </Page>
    </Document>
  )
}
