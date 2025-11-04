import { Link } from 'react-router-dom';

import Aside from './aside.jsx'
import Header from './header.jsx'
import Footer from './footer.jsx'

const Layout = ({children}) => {
  return (
    <div className="layout">
      <Aside/>
      <Header/>
      <main className='main'>
        {children}
      </main>
      <Footer/>
    </div>
  )
}

export default Layout