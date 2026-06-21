import api from '@api/client'
import { useUser } from '@context/UserContext.jsx'
import '@styles/header.css'

function Header() {
  const { loggedIn, isPending } = useUser()

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
    } finally {
      localStorage.removeItem('token')
      window.location.href = '/'
    }
  }

  return (
    <header className="header">
      <div className="header-inner">
        <a href="/" className="logo">
          <span className="logo-emoji">🧑‍💻</span>
          WithCoworkers
        </a>
        <nav className="nav">
          <a href="#features">기능</a>
          <a href="#how-it-works">사용법</a>
        </nav>
        <div className="header-actions">
          {loggedIn ? (
            <>
              {isPending ? (
                <span className="btn-header-pending">가입 승인 대기중</span>
              ) : (
                <>
                  <a href="/dashboard" className="btn-header">팀원 평가</a>
                  <a href="/result" className="btn-header">내 평가</a>
                </>
              )}
              <button className="btn-logout" onClick={handleLogout}>로그아웃</button>
            </>
          ) : (
            <a href="/login" className="btn-signup">로그인</a>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
