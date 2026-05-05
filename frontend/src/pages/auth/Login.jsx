import '@styles/login.css'

function Login() {
  return (
    <div className="login-page">
      <div className="login-card">
        <a href="/" className="login-logo">🧑‍💻 WithCoworker</a>
        <h1 className="login-title">안녕하세요 👋</h1>
        <p className="login-desc">구글 계정으로 바로 시작할 수 있어요</p>

        <a href="/oauth2/authorization/google" className="btn-google">
          <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="google" className="google-icon" />
          Google로 회원가입 / 로그인
        </a>
      </div>
    </div>
  )
}

export default Login
