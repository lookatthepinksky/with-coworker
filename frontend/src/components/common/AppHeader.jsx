import { Link } from 'react-router-dom'
import api from '@api/client'
import { useUser } from '@context/UserContext.jsx'
import '@styles/dashboard-header.css'

function AppHeader() {
  const { userName } = useUser()

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
    } finally {
      localStorage.removeItem('token')
      window.location.href = '/'
    }
  }

  return (
    <header className="dashboard-header">
      <div className="dashboard-header-inner">
        <Link to="/" className="dashboard-logo">🧑‍💻 WithCoworker</Link>
        <div className="dashboard-user">
          <span className="user-name">{userName || '...'}님, 반갑습니다.</span>
          <button className="dashboard-btn-logout" onClick={handleLogout}>로그아웃</button>
        </div>
      </div>
    </header>
  )
}

export default AppHeader
