import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@api/client'
import DashboardHeader from './components/DashboardHeader.jsx'
import '@styles/dashboard.css'

function Dashboard() {
  const navigate = useNavigate()
  const [userName, setUserName] = useState('')
  const [teamName, setTeamName] = useState('')
  const [teammates, setTeammates] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    if (token) {
      localStorage.setItem('token', token)
      window.history.replaceState({}, '', '/dashboard')
    }
  }, [])

  useEffect(() => {
    api.get('/api/dashboard')
      .then(({ data }) => {
        setUserName(data.userName || '')
        setTeamName(data.teamName || '')
        setTeammates(data.teammates)
        setLoading(false)
      })
      .catch((err) => {
        if (err.response?.status === 401 || err.response?.status === 403) {
          navigate('/')
        }
      })
  }, [navigate])

  const doneCount = teammates.filter((t) => t.done).length

  if (loading) return null

  return (
    <>
      <DashboardHeader />
      <main className="dashboard-main">

        <div className="page-nav">
          <a href="/dashboard" className="page-nav-btn active">팀원 평가</a>
          <a href="/result" className="page-nav-btn">내 평가</a>
        </div>

        <div className="dashboard-greeting">
          {teamName && <span className="team-badge">🏷️ {teamName}</span>}
          <h1>안녕하세요, {userName}님 👋</h1>
          <p>이번 달 팀원 평가를 완료해주세요</p>
        </div>

        <div className="dashboard-block">
          <h2 className="block-title">내가 한 평가</h2>

          <div className="dashboard-stats">
            <div className="stat-card">
              <span className="stat-card-label">전체 팀원</span>
              <span className="stat-card-value">{teammates.length}명</span>
            </div>
            <div className="stat-card accent">
              <span className="stat-card-label">평가 완료</span>
              <span className="stat-card-value">{doneCount}명</span>
            </div>
            <div className="stat-card">
              <span className="stat-card-label">남은 평가</span>
              <span className="stat-card-value">{teammates.length - doneCount}명</span>
            </div>
          </div>

          <section className="dashboard-section">
            <h2 className="section-label">이번 달 팀원 평가</h2>
            <div className="teammate-list">
              {teammates.map((t) => (
                <div className="teammate-card" key={t.id}>
                  <div className="teammate-info">
                    <span className="teammate-avatar">{t.name[0]}</span>
                    <div>
                      <span className="teammate-name">{t.name}</span>
                      {!t.done && (
                        <p className="teammate-waiting">
                          {t.name}님이 {userName}님의 평가를 기다리고 있어요! 👀
                        </p>
                      )}
                    </div>
                  </div>
                  {t.done ? (
                    <span className="badge-done">완료 ✓</span>
                  ) : (
                    <a href={`/evaluate/${t.id}`} className="btn-evaluate">평가하기</a>
                  )}
                </div>
              ))}
            </div>
          </section>
        </div>


      </main>
    </>
  )
}

export default Dashboard
