import { useState } from 'react'
import { useParams } from 'react-router-dom'
import DashboardHeader from '@pages/dashboard/components/DashboardHeader.jsx'
import '@styles/evaluate.css'

const teammates = [
  { id: '1', name: '이병헌' },
  { id: '2', name: '송강호' },
  { id: '3', name: '전지현' },
  { id: '4', name: '공유' },
  { id: '5', name: '박보검' },
]

const items = [
  { key: 'communication', label: '🗣️ 의사소통', desc: '의견을 명확하게 전달하고 경청하나요?' },
  { key: 'knowledge', label: '📚 지식공유', desc: '알고 있는 것을 팀원과 잘 공유하나요?' },
  { key: 'proactive', label: '🙋 적극성', desc: '먼저 나서고 주도적으로 행동하나요?' },
  { key: 'problem', label: '🔧 문제해결', desc: '문제 상황에서 해결책을 잘 찾나요?' },
  { key: 'deadline', label: '⏱️ 일정 준수', desc: '마감을 잘 지키나요?' },
  { key: 'accuracy', label: '🎯 정확성', desc: '실수 없이 꼼꼼하게 작업하나요?' },
]

function Evaluate() {
  const { id } = useParams()
  const teammate = teammates.find((t) => t.id === id)
  const [scores, setScores] = useState({})
  const [comment, setComment] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const handleScore = (key, value) => {
    setScores((prev) => ({ ...prev, [key]: value }))
  }

  const isAllScored = items.every((item) => scores[item.key])

  const handleSubmit = () => {
    if (!isAllScored) return
    setSubmitted(true)
    setTimeout(() => {
      window.location.href = '/dashboard'
    }, 1500)
  }

  if (submitted) {
    return (
      <div className="evaluate-done">
        <span className="done-emoji">🎉</span>
        <h2>평가 완료!</h2>
        <p>대시보드로 돌아갈게요...</p>
      </div>
    )
  }

  return (
    <>
      <DashboardHeader />
      <main className="evaluate-main">

        <div className="evaluate-top">
          <a href="/dashboard" className="btn-back">← 대시보드로</a>
          <div className="evaluate-target">
            <span className="evaluate-avatar">{teammate?.name[0]}</span>
            <div>
              <h1 className="evaluate-title">{teammate?.name}님 평가하기</h1>
              <p className="evaluate-desc">항목별로 1~5점을 선택해주세요</p>
            </div>
          </div>
        </div>

        <div className="evaluate-items">
          {items.map((item) => (
            <div className="evaluate-card" key={item.key}>
              <div className="evaluate-item-info">
                <span className="evaluate-item-label">{item.label}</span>
                <span className="evaluate-item-desc">{item.desc}</span>
              </div>
              <div className="score-buttons">
                {[1, 2, 3, 4, 5].map((num) => (
                  <button
                    key={num}
                    className={`score-btn ${scores[item.key] === num ? 'active' : ''}`}
                    onClick={() => handleScore(item.key, num)}
                  >
                    {num}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="evaluate-comment">
          <label>💬 한마디 남기기 <span className="optional">(선택)</span></label>
          <textarea
            placeholder="익명으로 전달돼요. 솔직하게 써도 괜찮아요 😊"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>

        <button
          className={`btn-submit ${isAllScored ? 'active' : ''}`}
          onClick={handleSubmit}
          disabled={!isAllScored}
        >
          {isAllScored ? '평가 제출하기 🚀' : `${items.length - Object.keys(scores).length}개 항목이 남았어요`}
        </button>

      </main>
    </>
  )
}

export default Evaluate
