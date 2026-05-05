import api from '@api/client'
import '@styles/dashboard-header.css'

function DashboardHeader({ userName }) {
  const handleLogout = async () => {
    try {
      await api.post('/logout')
    } catch {
      // Spring Security가 302를 응답해 인터셉터에서 에러로 처리될 수 있음 — 무시하고 홈으로 이동
    }
    window.location.href = '/'
  }

  const handleDeleteAccount = async () => {
    if (!window.confirm('정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.')) return
    await api.delete('/api/user/me')
    window.location.href = '/'
  }

  return (
    <header className="dashboard-header">
      <div className="dashboard-header-inner">
        <a href="/" className="logo">🧑‍💻 WithCoworker</a>
        <div className="dashboard-user">
          <span className="user-name">{userName || '...'}</span>
          <button className="btn-logout" onClick={handleLogout}>로그아웃</button>
          <button className="btn-withdraw" onClick={handleDeleteAccount}>탈퇴</button>
        </div>
      </div>
    </header>
  )
}

export default DashboardHeader
