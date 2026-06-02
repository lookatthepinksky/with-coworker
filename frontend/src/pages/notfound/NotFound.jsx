import { useNavigate } from 'react-router-dom'
import '@styles/notfound.css'

function NotFound() {
  const navigate = useNavigate()

  return (
    <div className="notfound-page">
      <div className="notfound-card">
        <div className="notfound-code">404</div>
        <h1 className="notfound-title">페이지를 찾을 수 없습니다</h1>
        <p className="notfound-desc">
          요청하신 페이지가 존재하지 않거나 이동되었습니다.<br />
          URL을 다시 확인해 주세요.
        </p>
        <div className="notfound-actions">
          <button className="btn-primary" onClick={() => navigate('/')}>
            홈으로 돌아가기
          </button>
          <button className="btn-secondary" onClick={() => navigate(-1)}>
            이전 페이지
          </button>
        </div>
      </div>
    </div>
  )
}

export default NotFound
