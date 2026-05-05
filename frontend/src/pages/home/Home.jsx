import Header from '@components/Header.jsx'
import Hero from './components/Hero.jsx'
import Features from './components/Features.jsx'
import HowItWorks from './components/HowItWorks.jsx'
import Footer from '@components/Footer.jsx'

function Home() {
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
