
const steps = [
  { step: '01', title: '로그인해요', desc: '구글 계정으로 로그인하면 바로 시작할 수 있어요.' },
  { step: '02', title: '팀을 선택해요', desc: '내가 속한 팀을 선택해요. 팀원들이랑 같이 평가할 수 있어요.' },
  { step: '03', title: '평가해요', desc: '팀원을 항목별로 익명으로 평가해요. 솔직하게 써도 돼요. 진짜로요.' },
  { step: '04', title: '확인해요', desc: '내가 받은 평가를 확인하고 "아 내가 이렇게 보이는구나" 하면 됩니다 😅' },
]

function HowItWorks() {
  return (
    <section className="how-it-works" id="how-it-works">
      <div className="how-inner">
        <h2 className="section-title">어떻게 쓰는 거예요?</h2>
        <p className="section-desc">4단계밖에 없어요. 진짜 쉬워요</p>
        <div className="steps">
          {steps.map((s) => (
            <div className="step" key={s.step}>
              <div className="step-number">{s.step}</div>
              <h3 className="step-title">{s.title}</h3>
              <p className="step-desc">{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

export default HowItWorks
