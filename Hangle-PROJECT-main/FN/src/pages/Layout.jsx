import { Outlet } from "react-router-dom"; {/*추가 */}
import Aside from './aside.jsx'
import Header from './header.jsx'
import Footer from './footer.jsx'

const Layout = ({children}) => {
  return (
    <div className="layout">
      <Aside/>
      <Header/>
      <main className='main'>
        <Outlet/> {/*추가 */}
        {children}
      </main>
      <Footer/>
    </div>
  )
}

export default Layout