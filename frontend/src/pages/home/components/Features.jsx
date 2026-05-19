
const features = [
  {
    icon: '🕵️',
    badge: 'anonymous: true',
    title: '철저한 익명',
    desc: '누가 뭐라고 했는지 절대 안 나와요. 진짜로요. 코드로 보장합니다.',
  },
  {
    icon: '📝',
    badge: 'items: [...]',
    title: '항목별 평가',
    desc: '업무 능력, 협업, 커뮤니케이션… 두루뭉술하지 않게 항목별로 평가해요.',
  },
  {
    icon: '📈',
    badge: 'result.render()',
    title: '내 평가 결과 보기',
    desc: '남들이 나를 어떻게 보는지 차트로 한눈에 확인할 수 있어요. 두근두근 🫀',
  },
  {
    icon: '🔐',
    badge: 'access: internal',
    title: '우리 팀만의 공간',
    desc: '외부에 공개되지 않아요. 우리 회사 팀원들끼리만 사용하는 아늑한 공간이에요.',
  },
]

function Features() {
  return (
    <section className="features" id="features">
      <div className="features-inner">
        <h2 className="section-title">이런 기능들이 있어요</h2>
        <p className="section-desc">복잡한 거 없어요. 딱 필요한 것만 있습니다</p>
        <div className="features-grid">
          {features.map((f) => (
            <div className="feature-card" key={f.title}>
              <span className="feature-icon">{f.icon}</span>
              <span className="feature-badge">{f.badge}</span>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

export default Features
