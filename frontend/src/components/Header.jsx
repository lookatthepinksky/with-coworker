import { Link } from 'react-router-dom'
import api from '@api/client'
import { useUser } from '@context/UserContext.jsx'
import '@styles/header.css'

function Header() {
  const { loggedIn, isPending } = useUser()

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
    } finally {
      window.location.href = '/'
    }
  }

  return (
    <header className="header">
      <div className="header-inner">
        <Link to="/" className="logo">
          <span className="logo-emoji">🧑‍💻</span>
          WithCoworkers
        </Link>
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
                  <Link to="/team-members/overview" className="btn-header">팀원 평가</Link>
                  <Link to="/result" className="btn-header">내 평가</Link>
                </>
              )}
              <button className="btn-logout" onClick={handleLogout}>로그아웃</button>
            </>
          ) : (
            <Link to="/login" className="btn-signup">로그인</Link>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
