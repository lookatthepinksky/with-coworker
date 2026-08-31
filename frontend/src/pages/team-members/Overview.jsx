import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '@api/client'
import AppHeader from '@components/common/AppHeader.jsx'
import '@styles/dashboard.css'

function PendingModal({ members, onApprove, onReject, onClose }) {
  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2 className="modal-title">가입 승인 요청</h2>
        <p className="modal-desc">아래 팀원이 가입 승인을 기다리고 있어요</p>

        <div className="modal-member-list">
          {members.map((m) => (
            <div className="modal-member-item" key={m.teamMemberId}>
              <div className="modal-member-info">
                <span className="modal-member-name">{m.name}</span>
                <span className="modal-member-email">{m.email}</span>
              </div>
              <div className="modal-member-actions">
                <button className="btn-approve" onClick={() => onApprove(m.teamMemberId)}>승인</button>
                <button className="btn-reject" onClick={() => onReject(m.teamMemberId)}>거절</button>
              </div>
            </div>
          ))}
        </div>

        <div className="modal-footer">
          <button className="btn-modal-close" onClick={onClose}>나중에 처리</button>
        </div>
      </div>
    </div>
  )
}

function TeamMembersOverview() {
  const navigate = useNavigate()
  const [userName, setUserName] = useState('')
  const [teamName, setTeamName] = useState('')
  const [teammates, setTeammates] = useState([])
  const [loading, setLoading] = useState(true)
  const [isAdmin, setIsAdmin] = useState(false)
  const [pendingMembers, setPendingMembers] = useState([])
  const [showPendingModal, setShowPendingModal] = useState(false)

  const targetMonth = (() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 1)
    return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`
  })()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    if (token) {
      localStorage.setItem('token', token)
      window.history.replaceState({}, '', '/team-members/overview')
    }
  }, [])

  useEffect(() => {
    const fetchData = async () => {
      try {
        const { data } = await api.get('/api/my/team-members/overview', { params: { target_month: targetMonth } })
        setUserName(data.userName || '')
        setTeamName(data.teamName || '')
        setTeammates(data.teammates)
        setIsAdmin(data.isAdmin || false)
        setLoading(false)

        if (data.isAdmin) {
          const { data: pending } = await api.get('/api/teams/pending')
          if (pending.length > 0) {
            setPendingMembers(pending)
            setShowPendingModal(true)
          }
        }
      } catch (err) {
        if (err.response?.status === 401 || err.response?.status === 403) {
          navigate('/')
        }
      }
    }
    fetchData()
  }, [navigate])

  const refreshTeammates = async () => {
    const { data } = await api.get('/api/my/team-members/overview', { params: { target_month: targetMonth } })
    setTeammates(data.teammates)
  }

  const handleApprove = async (teamMemberId) => {
    await api.post(`/api/teams/members/${teamMemberId}/approve`)
    const updated = pendingMembers.filter((m) => m.teamMemberId !== teamMemberId)
    setPendingMembers(updated)
    if (updated.length === 0) setShowPendingModal(false)
    refreshTeammates()
  }

  const handleReject = async (teamMemberId) => {
    await api.delete(`/api/teams/members/${teamMemberId}`)
    const updated = pendingMembers.filter((m) => m.teamMemberId !== teamMemberId)
    setPendingMembers(updated)
    if (updated.length === 0) setShowPendingModal(false)
  }

  const doneCount = teammates.filter((t) => t.done).length

  const evalDate = new Date()
  evalDate.setMonth(evalDate.getMonth() - 1)
  const evalYear = evalDate.getFullYear()
  const evalMonth = evalDate.getMonth() + 1

  if (loading) return null

  return (
    <>
      <AppHeader />

      {showPendingModal && (
        <PendingModal
          members={pendingMembers}
          onApprove={handleApprove}
          onReject={handleReject}
          onClose={() => setShowPendingModal(false)}
        />
      )}

      <main className="dashboard-main">

        <div className="page-nav">
          <Link to="/team-members/overview" className="page-nav-btn active">팀원 평가</Link>
          <Link to="/result" className="page-nav-btn">내 평가</Link>
        </div>

        <div className="dashboard-greeting">
          {teamName && <span className="team-badge">🏷️ {teamName}{isAdmin && ' · 팀장'}</span>}
          <h1>안녕하세요, {userName}님 👋</h1>
          <p className="greeting-status">
            <span className="greeting-month">{evalYear}년 {evalMonth}월</span>
            {' '}팀원 평가를 {teammates.length > 0 && doneCount === teammates.length ? '완료하였습니다 ✓' : '완료해주세요'}
          </p>
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
            <h2 className="section-label">
              이번 달 팀원 평가
              {isAdmin && pendingMembers.length > 0 && (
                <button
                  className="section-sub"
                  style={{ marginLeft: '8px', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--primary)', fontWeight: '700' }}
                  onClick={() => setShowPendingModal(true)}
                >
                  승인 대기 {pendingMembers.length}명
                </button>
              )}
            </h2>
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
                    <Link to={`/evaluate/${t.id}`} className="btn-evaluate">평가하기</Link>
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

export default TeamMembersOverview
