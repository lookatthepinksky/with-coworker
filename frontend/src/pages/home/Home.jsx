import { useEffect } from 'react'
import Header from '@components/Header.jsx'
import Hero from './components/Hero.jsx'
import Features from './components/Features.jsx'
import HowItWorks from './components/HowItWorks.jsx'
import Footer from '@components/Footer.jsx'
import api from '@api/client.js'

function Home() {
  useEffect(() => {
    api.post('/api/visitor').catch(() => {})
  }, [])

  return (
    <>
      <Header />
      <main>
        <Hero />
        <Features />
        <HowItWorks />
      </main>
      <Footer />
    </>
  )
}

export default Home
