import { useState, useEffect, useRef } from 'react'
import api from '@api/client'

const EXPIRE_SECONDS = 5 * 60

function Signup() {
  const [form, setForm] = useState({ name: '', loginId: '', email: '', password: '', passwordConfirm: '' })
  const [emailCode, setEmailCode] = useState('')
  const [emailSent, setEmailSent] = useState(false)
  const [emailVerified, setEmailVerified] = useState(false)
  const [sending, setSending] = useState(false)
  const [verifying, setVerifying] = useState(false)
  const [error, setError] = useState('')
  const [timeLeft, setTimeLeft] = useState(0)
  const timerRef = useRef(null)

  useEffect(() => {
    if (!emailSent || emailVerified) return

    setTimeLeft(EXPIRE_SECONDS)
    clearInterval(timerRef.current)
    timerRef.current = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timerRef.current)
  }, [emailSent, emailVerified])

  const formatTime = (seconds) => {
    const m = String(Math.floor(seconds / 60)).padStart(2, '0')
    const s = String(seconds % 60).padStart(2, '0')
    return `${m}:${s}`
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    if (e.target.name === 'email') {
      setEmailSent(false)
      setEmailVerified(false)
      setEmailCode('')
      clearInterval(timerRef.current)
      setTimeLeft(0)
    }
  }

  const handleSendEmail = async () => {
    if (!form.email) return
    setSending(true)
    setError('')
    try {
      await api.post('/api/auth/email/send', { email: form.email })
      setEmailSent(true)
      setEmailCode('')
    } catch {
      setError('인증번호 발송에 실패했습니다. 이메일을 확인해주세요.')
    } finally {
      setSending(false)
    }
  }

  const handleVerifyCode = async () => {
    if (!emailCode) return
    setVerifying(true)
    setError('')
    try {
      await api.post('/api/auth/email/verify', { email: form.email, code: emailCode })
      setEmailVerified(true)
    } catch (err) {
      if (err.response) {
        setError('인증번호가 일치하지 않습니다.')
      } else {
        setError('인증 확인에 실패했습니다.')
      }
    } finally {
      setVerifying(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }
    if (!emailVerified) {
      setError('이메일 인증을 완료해주세요.')
      return
    }
    setError('')
    try {
      await api.post('/api/auth/signup', {
        name: form.name,
        loginId: form.loginId,
        email: form.email,
        password: form.password,
      })
      window.location.href = '/login'
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.')
    }
  }

  return (
    <div className="signup-page">
      <div className="signup-card">
        <a href="/" className="signup-logo">🧑‍💻 WithCoworker</a>
        <h1 className="signup-title">처음 오셨군요 👋</h1>
        <p className="signup-desc">팀원들과 함께 시작해보세요</p>

        <form className="signup-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>이름</label>
            <input type="text" name="name" placeholder="이름을 입력하세요" value={form.name} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>아이디</label>
            <input type="text" name="loginId" placeholder="아이디를 입력하세요 (4~20자)" value={form.loginId} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>이메일</label>
            <div className="input-row">
              <input
                type="email"
                name="email"
                placeholder="이메일을 입력하세요"
                value={form.email}
                onChange={handleChange}
                disabled={emailVerified}
                required
              />
              <button
                type="button"
                className="btn-send"
                onClick={handleSendEmail}
                disabled={!form.email || sending || emailVerified}
              >
                {emailVerified ? '인증완료' : sending ? '발송중...' : emailSent ? '재발송' : '인증번호 발송'}
              </button>
            </div>
          </div>

          {emailSent && !emailVerified && (
            <div className="form-group">
              <label>
                인증번호
                {timeLeft > 0
                  ? <span className={`timer ${timeLeft <= 60 ? 'timer-warn' : ''}`}> {formatTime(timeLeft)}</span>
                  : <span className="timer timer-expired"> 만료됨</span>
                }
              </label>
              <div className="input-row">
                <input
                  type="text"
                  placeholder="6자리 인증번호 입력"
                  value={emailCode}
                  onChange={(e) => setEmailCode(e.target.value)}
                  maxLength={6}
                  disabled={timeLeft === 0}
                />
                <button
                  type="button"
                  className="btn-verify"
                  onClick={handleVerifyCode}
                  disabled={emailCode.length !== 6 || verifying || timeLeft === 0}
                >
                  {verifying ? '확인중...' : '인증 확인'}
                </button>
              </div>
              {timeLeft === 0 && (
                <p className="error-msg">인증번호가 만료되었습니다. 재발송해주세요.</p>
              )}
            </div>
          )}

          {emailVerified && (
            <p className="verified-msg">✅ 이메일 인증이 완료되었습니다.</p>
          )}

          <div className="form-group">
            <label>비밀번호</label>
            <input type="password" name="password" placeholder="비밀번호를 입력하세요 (8자 이상)" value={form.password} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>비밀번호 확인</label>
            <input type="password" name="passwordConfirm" placeholder="비밀번호를 다시 입력하세요" value={form.passwordConfirm} onChange={handleChange} required />
          </div>

          {error && <p className="error-msg">{error}</p>}

          <button type="submit" className="signup-btn">회원가입</button>
        </form>

        <p className="signup-footer">
          이미 계정이 있으신가요? <a href="/login">로그인</a>
        </p>
      </div>
    </div>
  )
}

export default Signup
