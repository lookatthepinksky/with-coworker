import { useUser } from '@context/UserContext.jsx'
import '@styles/hero.css'

function Hero() {
  const { loggedIn } = useUser()

  return (
    <section className="hero">
      <div className="hero-inner">
        <span className="hero-badge">// 익명 보장됨 ✓</span>
        <h1 className="hero-title">
          팀원한테 솔직해질<br />
          <em>용기가 생겼어요</em> 👀
        </h1>
        <p className="hero-desc">
          하고 싶은 말 있었는데 못 했죠?<br />
          WithCoworker에서는 익명으로 솔직하게 평가할 수 있어요.<br />
          우리 팀만을 위한 아늑한 피드백 공간이에요.
        </p>
        <div className="hero-buttons">
          {loggedIn ? (
            <a href="/dashboard" className="btn-primary">팀원 평가 시작하기 🚀</a>
          ) : (
            <a href="/login" className="btn-primary">로그인하고 시작하기 🚀</a>
          )}
          <a href="#how-it-works" className="btn-secondary">어떻게 쓰는 건데?</a>
        </div>
      </div>
    </section>
  )
}

export default Hero
