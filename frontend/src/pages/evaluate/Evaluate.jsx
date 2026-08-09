import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import api from '@api/client'
import AppHeader from '@components/common/AppHeader.jsx'
import '@styles/evaluate.css'


function Evaluate() {
  const { id } = useParams()
  const [name, setName] = useState('')
  const [items, setItems] = useState([])
  const [scores, setScores] = useState({})
  const [comment, setComment] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [correcting, setCorrecting] = useState(false)
  const [corrected, setCorrected] = useState(false)
  const [commentWarning, setCommentWarning] = useState('')
  const [originalComment, setOriginalComment] = useState('')
  const [showOriginal, setShowOriginal] = useState(false)
  const [aiRemaining, setAiRemaining] = useState(null)
  const [aiLimit, setAiLimit] = useState(null)

  useEffect(() => {
    api.get(`/api/evaluate/${id}`)
      .then(({ data }) => setName(data.name))
      .catch(() => {
        alert('잘못된 접근입니다.')
        window.location.href = '/team-members/overview'
      })
    api.get('/api/evaluation-items').then(({ data }) => setItems(data))
    api.get(`/api/ai/usage?evaluateeId=${id}`)
      .then(({ data }) => { setAiRemaining(data.remaining); setAiLimit(data.limit) })
      .catch(() => {})
  }, [id])

  const handleCorrect = async () => {
    if (!comment.trim() || correcting || aiRemaining === 0) return
    setCorrecting(true)
    try {
      const { data } = await api.post('/api/ai/correct-comment', {
        comment,
        evaluateeId: String(id),
      })
      if (data.error === 'LIMIT_EXCEEDED') {
        setAiRemaining(0)
        setCommentWarning(`${name}님 평가의 이번 달 AI 교정 횟수를 모두 소진했어요.`)
        return
      }
      if (data.error === 'TOO_LONG') {
        setCommentWarning('150자를 초과한 내용은 AI 교정을 사용할 수 없어요.')
        return
      }
      if (data.error === 'SERVICE_UNAVAILABLE') {
        setCommentWarning('일시적으로 AI 교정을 사용할 수 없어요. 잠시 후 다시 시도해주세요.')
        return
      }
      if (data.error === 'CREDIT_EXCEEDED') {
        setCommentWarning('AI 교정 서비스를 현재 사용할 수 없어요. 관리자에게 문의해주세요.')
        return
      }
      else {
        setOriginalComment(comment)
        setComment(data.result)
        setCorrected(true)
        setShowOriginal(true)
      }
      setAiRemaining((prev) => Math.max(0, (prev ?? 0) - 1))
    } finally {
      setCorrecting(false)
    }
  }

  const handleScore = (itemId, value) => {
    setScores((prev) => ({ ...prev, [itemId]: value }))
  }

  const commentLen = comment.trim().length
  const isAllScored = items.length > 0 && items.every((item) => scores[item.id]) && commentLen > 0 && commentLen <= 150

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
      setTimeout(() => { window.location.href = '/team-members/overview' }, 1500)
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

  const aiExhausted = aiRemaining === 0
  const aiUsed = aiLimit !== null && aiRemaining !== null ? aiLimit - aiRemaining : null

  return (
    <>
      <AppHeader />
      <main className="evaluate-main">

        <div className="evaluate-top">
          <a href="/team-members/overview" className="btn-back">← 팀원 평가로</a>
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
            <span className={`comment-count ${commentLen > 150 ? 'over' : ''}`}>
              {comment.length} / 150자 이내
            </span>
          </label>
          <textarea
            placeholder="건설적인 피드백은 팀 전체를 성장시켜요 ✍️"
            value={comment}
            onChange={(e) => { setComment(e.target.value); setCorrected(false); setCommentWarning('') }}
          />
          {(corrected || commentWarning) && (
            <p className={commentWarning ? 'comment-warning' : 'comment-corrected'}>
              {commentWarning || (
                <>
                  ✨ AI 교정 완료.{' '}
                  <button className="btn-view-original" onClick={() => setShowOriginal(true)}>원본 보기</button>
                </>
              )}
            </p>
          )}
          <div className="comment-actions">
            {aiUsed !== null && (
              <span className={`ai-usage-badge ${aiExhausted ? 'exhausted' : ''}`}>
                {aiExhausted ? `소진 (${aiLimit}/${aiLimit})` : `교정 ${aiUsed}/${aiLimit}회`}
              </span>
            )}
            <button
              className={`btn-ai-correct ${correcting ? 'loading' : ''}`}
              onClick={handleCorrect}
              disabled={commentLen === 0 || commentLen > 150 || correcting || corrected || aiExhausted}
            >
              {correcting
                ? '교정 중...'
                : aiExhausted
                  ? `AI 교정 소진 (${aiLimit}/${aiLimit})`
                  : '✨ AI 교정'}
            </button>
          </div>
          {aiExhausted && (
            <p className="comment-warning">{`${name}님 평가의 이번 달 AI 교정 횟수를 모두 소진했어요.`}</p>
          )}
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
                : '한마디를 남겨주세요'}
        </button>

      </main>

      {showOriginal && (
        <div className="original-overlay" onClick={() => setShowOriginal(false)}>
          <div className="original-popup" onClick={(e) => e.stopPropagation()}>
            <div className="original-popup-header">
              <span>종합 의견 원본 내용</span>
              <button className="btn-close-original" onClick={() => setShowOriginal(false)}>✕</button>
            </div>
            <p className="original-popup-text">{originalComment}</p>
            <button
              className="btn-restore-original"
              onClick={() => {
                setComment(originalComment)
                setCorrected(false)
                setOriginalComment('')
                setCommentWarning('')
                setShowOriginal(false)
              }}
            >
              원본으로 되돌리기
            </button>
          </div>
        </div>
      )}
    </>
  )
}

export default Evaluate
