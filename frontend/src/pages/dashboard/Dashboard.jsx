import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@api/client'
import DashboardHeader from './components/DashboardHeader.jsx'
import '@styles/dashboard.css'

function Dashboard() {
  const navigate = useNavigate()
  const [animated, setAnimated] = useState(false)
  const [userName, setUserName] = useState('')
  const [teamName, setTeamName] = useState('')
  const [teammates, setTeammates] = useState([])
  const [myScores, setMyScores] = useState([])
  const [loading, setLoading] = useState(true)


  useEffect(() => {
    api.get('/api/dashboard')
      .then(({ data }) => {
        setUserName(data.userName || '')
        setTeamName(data.teamName || '')
        setTeammates(data.teammates)
        setMyScores(data.myScores)
        setLoading(false)
        setTimeout(() => setAnimated(true), 100)
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
      <DashboardHeader userName={userName} />
      <main className="dashboard-main">

        <div className="dashboard-greeting">
          {teamName && <span className="team-badge">🏷️ {teamName}</span>}
          <h1>안녕하세요, {userName}님 👋</h1>
          <p>이번 달 팀원 평가를 완료해주세요</p>
        </div>

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

        <div className="dashboard-content">
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

          <section className="dashboard-section">
            <h2 className="section-label">내 평가 결과 <span className="section-sub">누적</span></h2>
            {myScores.length === 0 ? (
              <p className="no-result">아직 받은 평가가 없어요</p>
            ) : (
              <>
                <div className="score-list">
                  {myScores.map((s) => (
                    <div className="score-item" key={s.label}>
                      <div className="score-top">
                        <span className="score-label">{s.label}</span>
                        <span className="score-value">{s.score}</span>
                      </div>
                      <div className="score-bar-bg">
                        <div className="score-bar-fill" style={{ width: animated ? `${(s.score / 5) * 100}%` : '0%' }} />
                      </div>
                    </div>
                  ))}
                </div>
                <a href="/result" className="btn-result">자세히 보기 →</a>
              </>
            )}
          </section>
        </div>

      </main>
    </>
  )
}

export default Dashboard
