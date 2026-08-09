import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@api/client'

function TeamSelect() {
  const [teams, setTeams] = useState([])
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(false)
  const [mode, setMode] = useState('select') // 'select' | 'create' | 'pending'
  const [newTeamName, setNewTeamName] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const pending = params.get('pending')
    if (pending === 'true') setMode('pending')
    if (pending) window.history.replaceState({}, '', '/team-select')
  }, [])

  useEffect(() => {
    if (mode !== 'select') return
    api.get('/api/teams').then(({ data }) => setTeams(data))
  }, [mode])

  const handleJoin = async () => {
    if (!selected) return
    setLoading(true)
    setError('')
    try {
      await api.post(`/api/teams/${selected}/join`)
      setMode('pending')
    } catch (e) {
      setError(e.response?.data?.message || '가입 요청에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    if (!newTeamName.trim()) return
    setLoading(true)
    setError('')
    try {
      await api.post('/api/teams', { name: newTeamName.trim() })
      navigate('/team-members/overview')
    } catch (e) {
      setError(e.response?.data?.message || '팀 생성에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  if (mode === 'pending') {
    return (
      <div className="team-select-page">
        <div className="team-select-card">
          <a href="/" className="login-logo">🧑‍💻 WithCoworker</a>
          <div style={{ fontSize: '3rem', marginBottom: '16px' }}>⏳</div>
          <h1 className="team-select-title">승인 대기 중이에요</h1>
          <p className="team-select-desc">
            팀장이 가입 요청을 확인 중이에요.<br />
            승인되면 로그인 후 바로 시작할 수 있어요!
          </p>
          <a href="/" className="btn-confirm" style={{ textAlign: 'center', display: 'block', lineHeight: '1' }}>
            홈으로
          </a>
        </div>
      </div>
    )
  }

  if (mode === 'create') {
    return (
      <div className="team-select-page">
        <div className="team-select-card">
          <a href="/" className="login-logo">🧑‍💻 WithCoworker</a>
          <h1 className="team-select-title">팀 만들기</h1>
          <p className="team-select-desc">새 팀을 만들고 팀원을 초대해보세요</p>

          <div className="team-list">
            <input
              className="login-input"
              style={{ width: '100%' }}
              placeholder="팀 이름을 입력해주세요"
              value={newTeamName}
              onChange={(e) => setNewTeamName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
            />
          </div>

          {error && <p className="error-msg" style={{ marginBottom: '12px' }}>{error}</p>}

          <button
            className="btn-confirm"
            disabled={!newTeamName.trim() || loading}
            onClick={handleCreate}
          >
            {loading ? '생성 중...' : '팀 만들기'}
          </button>

          <button
            onClick={() => { setMode('select'); setError('') }}
            style={{ marginTop: '14px', background: 'none', border: 'none', color: 'var(--text-sub)', fontSize: '0.88rem', cursor: 'pointer' }}
          >
            돌아가기
          </button>
        </div>
      </div>
    )
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

        {error && <p className="error-msg" style={{ marginBottom: '12px' }}>{error}</p>}

        <button
          className="btn-confirm"
          disabled={!selected || loading}
          onClick={handleJoin}
        >
          {loading ? '가입 중...' : '시작하기'}
        </button>

        <div style={{ marginTop: '20px', borderTop: '1px solid var(--border)', paddingTop: '20px', width: '100%', textAlign: 'center' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-sub)' }}>팀이 없으신가요? </span>
          <button
            onClick={() => { setMode('create'); setError('') }}
            style={{ fontSize: '0.85rem', fontWeight: '700', color: 'var(--primary)', background: 'none', border: 'none', cursor: 'pointer' }}
          >
            새 팀 만들기
          </button>
        </div>
      </div>
    </div>
  )
}

export default TeamSelect
