import api from '@api/client'
import { useUser } from '@context/UserContext.jsx'
import '@styles/dashboard-header.css'

function DashboardHeader() {
  const { userName } = useUser()

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
    } finally {
      localStorage.removeItem('token')
      window.location.href = '/'
    }
  }

  const handleDeleteAccount = async () => {
    if (!window.confirm('정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.')) return
    await api.delete('/api/user/me')
    window.location.href = '/'
  }

  return (
    <header className="dashboard-header">
      <div className="dashboard-header-inner">
        <a href="/" className="dashboard-logo">🧑‍💻 WithCoworker</a>
        <div className="dashboard-user">
          <span className="user-name">{userName || '...'}님, 반갑습니다.</span>
          <button className="dashboard-btn-logout" onClick={handleLogout}>로그아웃</button>
          {/* <button className="btn-withdraw" onClick={handleDeleteAccount}>탈퇴</button> */}
        </div>
      </div>
    </header>
  )
}

export default DashboardHeader
