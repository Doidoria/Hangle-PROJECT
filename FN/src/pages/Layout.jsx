import { Outlet } from "react-router-dom";
import Aside from './aside.jsx'
import Header from './header.jsx'
import Footer from './footer.jsx'
import ChatWidget from "../components/ChatWidget.jsx";

const Layout = ({children}) => {
  return (
    <div className="layout">
      <Aside/>
      <Header/>
      <main className='main'>
        <Outlet/>
        {children}
      </main>
      <Footer/>
      <ChatWidget />
    </div>
  )
}

export default Layout