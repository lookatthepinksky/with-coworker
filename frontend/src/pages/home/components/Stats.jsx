import '@styles/stats.css'

const stats = [
  { value: '12,400+', label: '누적 회원' },
  { value: '58,000+', label: '누적 평가 수' },
  { value: '1,200+', label: '참여 기업' },
  { value: '4.8 / 5', label: '평균 만족도' },
]

function Stats() {
  return (
    <section className="stats">
      <div className="stats-inner">
        {stats.map((s) => (
          <div className="stat-item" key={s.label}>
            <span className="stat-value">{s.value}</span>
            <span className="stat-label">{s.label}</span>
          </div>
        ))}
      </div>
    </section>
  )
}

export default Stats
