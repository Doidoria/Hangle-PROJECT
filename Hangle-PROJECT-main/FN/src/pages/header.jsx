import '../css/header.scss'
import { SearchBox, ThemeToggle, Loginbtn, Profilebtn } from '../js/header-btn'


const Header = () => {

  return (
    <header className="topbar" aria-label="상단바">
      <SearchBox />
      <div className="top-actions">
        <ThemeToggle />
        <Loginbtn />
        <Profilebtn/>
      </div>
    </header>
  )
}
export default Header