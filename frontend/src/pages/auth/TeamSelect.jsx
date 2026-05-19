import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@api/client'

function TeamSelect() {
  const [teams, setTeams] = useState([])
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    if (token) {
      localStorage.setItem('token', token)
      window.history.replaceState({}, '', '/team-select')
    }
  }, [])

  useEffect(() => {
    api.get('/api/teams').then(({ data }) => setTeams(data))
  }, [])

  const handleJoin = async () => {
    if (!selected) return
    setLoading(true)
    await api.post(`/api/teams/${selected}/join`)
    navigate('/dashboard')
  }

  return (
    <div className="team-select-page">
      <div className="team-select-card">
        <a href="/" className="login-logo">🧑‍💻 WithCoworker</a>
        <h1 className="team-select-title">팀을 선택해주세요</h1>
        <p className="team-select-desc">소속된 팀을 고르면 바로 시작할 수 있어요</p>

        <div className="team-list">
          {teams.map((team) => (
            <button
              key={team.teamId}
              className={`team-item ${selected === team.teamId ? 'selected' : ''}`}
              onClick={() => setSelected(team.teamId)}
            >
              {team.name}
            </button>
          ))}
        </div>

        <button
          className="btn-confirm"
          disabled={!selected || loading}
          onClick={handleJoin}
        >
          {loading ? '가입 중...' : '시작하기'}
        </button>
      </div>
    </div>
  )
}

export default TeamSelect
