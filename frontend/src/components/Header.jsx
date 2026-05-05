import { useEffect, useState } from 'react'
import api from '@api/client'
import '@styles/header.css'

function Header() {
  const [loggedIn, setLoggedIn] = useState(false)

  useEffect(() => {
    api.get('/api/user/me')
      .then(({ data }) => setLoggedIn(!!data.loggedIn))
      .catch(() => setLoggedIn(false))
  }, [])

  return (
    <header className="header">
      <div className="header-inner">
        <a href="/" className="logo">
          <span className="logo-emoji">🧑‍💻</span>
          WithCoworker
        </a>
        <nav className="nav">
          <a href="#features">기능</a>
          <a href="#how-it-works">사용법</a>
        </nav>
        <div className="header-actions">
          {loggedIn ? (
            <a href="/dashboard" className="btn-signup">대시보드 →</a>
          ) : (
            <a href="/login" className="btn-signup">로그인</a>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
