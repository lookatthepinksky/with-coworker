import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@api/client'
import '@styles/login.css'

function Login() {
  const navigate = useNavigate()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/login', { loginId, password })
      window.location.href = res.data.redirectPath
    } catch (err) {
      if (err.response?.status === 404) {
        alert('존재하지 않는 아이디입니다.')
      } else {
        setError(err.response?.data?.message || '로그인에 실패했습니다.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <a href="/" className="login-logo">🧑‍💻 WithCoworker</a>
        <h1 className="login-title">안녕하세요 👋</h1>
        <p className="login-desc">계정으로 로그인하거나 구글로 시작하세요</p>

        <form className="login-form" onSubmit={handleLogin}>
          <input
            className="login-input"
            type="text"
            placeholder="아이디"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            autoComplete="username"
          />
          <input
            className="login-input"
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
          {error && <p className="login-error">{error}</p>}
          <button className="btn-login" type="submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="login-divider">
          <span>또는</span>
        </div>

        <a href="/oauth2/authorization/google" className="btn-google">
          <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="google" className="google-icon" />
          Google로 회원가입 / 로그인
        </a>
      </div>
    </div>
  )
}

export default Login
