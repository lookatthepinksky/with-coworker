import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import api from '@api/client'
import DashboardHeader from '@pages/dashboard/components/DashboardHeader.jsx'
import '@styles/evaluate.css'

function Evaluate() {
  const { id } = useParams()
  const [name, setName] = useState('')
  const [items, setItems] = useState([])
  const [scores, setScores] = useState({})
  const [comment, setComment] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    api.get(`/api/evaluate/${id}`).then(({ data }) => setName(data.name))
    api.get('/api/evaluation-items').then(({ data }) => setItems(data))
  }, [id])

  const handleScore = (itemId, value) => {
    setScores((prev) => ({ ...prev, [itemId]: value }))
  }

  const commentLen = comment.trim().length
  const isAllScored = items.length > 0 && items.every((item) => scores[item.id]) && commentLen >= 100 && commentLen <= 130

  const handleSubmit = async () => {
    if (!isAllScored || submitting) return
    setSubmitting(true)
    try {
      await api.post('/api/evaluations', {
        evaluateeId: Number(id),
        comment,
        scores: items.map((item) => ({ itemId: item.id, score: scores[item.id] })),
      })
      setSubmitted(true)
      setTimeout(() => { window.location.href = '/dashboard' }, 1500)
    } catch {
      setSubmitting(false)
    }
  }

  if (submitted) {
    return (
      <div className="evaluate-done">
        <span className="done-emoji">🎉</span>
        <h2>평가 완료!</h2>
        <p>팀원 평가로 돌아갈게요...</p>
      </div>
    )
  }

  return (
    <>
      <DashboardHeader />
      <main className="evaluate-main">

        <div className="evaluate-top">
          <a href="/dashboard" className="btn-back">← 팀원 평가로</a>
          <div className="evaluate-target">
            <span className="evaluate-avatar">{name ? name[0] : ''}</span>
            <div>
              <h1 className="evaluate-title">{name}님 평가하기</h1>
              <p className="evaluate-desc">항목별로 1~5점을 선택해주세요</p>
            </div>
          </div>
        </div>

        <div className="evaluate-items">
          {items.map((item) => (
              <div className="evaluate-card" key={item.id}>
                <div className="evaluate-item-info">
                  <span className="evaluate-item-label">{item.label}</span>
                  <span className="evaluate-item-desc">{item.description}</span>
                </div>
                <div className="score-buttons">
                  {[1, 2, 3, 4, 5].map((num) => (
                    <button
                      key={num}
                      className={`score-btn ${scores[item.id] === num ? 'active' : ''}`}
                      onClick={() => handleScore(item.id, num)}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
          ))}
        </div>

        <div className="evaluate-comment">
          <label>
            💬 종합 의견 남기기 <span className="required">*</span>
            <span className={`comment-count ${commentLen >= 100 && commentLen <= 130 ? 'done' : commentLen > 130 ? 'over' : ''}`}>
              {comment.length} / 100~130자
            </span>
          </label>
          <textarea
            placeholder="건설적인 피드백은 팀 전체를 성장시켜요 ✍️"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>

        <button
          className={`btn-submit ${isAllScored ? 'active' : ''}`}
          onClick={handleSubmit}
          disabled={!isAllScored || submitting}
        >
          {submitting
            ? '제출 중...'
            : isAllScored
              ? '평가 제출하기 🚀'
              : items.length - Object.keys(scores).length > 0
                ? `${items.length - Object.keys(scores).length}개 항목이 남았어요`
                : commentLen < 100
                  ? `종합 의견을 ${100 - commentLen}자 더 써주세요`
                  : commentLen > 130
                    ? `${commentLen - 130}자 줄여주세요`
                    : '한마디를 남겨주세요'}
        </button>

      </main>
    </>
  )
}

export default Evaluate
