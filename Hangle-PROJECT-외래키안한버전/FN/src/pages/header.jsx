import '../css/header.scss'
import { SearchBox, ThemeToggle, HeaderButtons, Profilebtn} from '../components/header-btn'

const Header = () => {

  return (
    <header className="topbar" aria-label="상단바">
      <SearchBox />
      <div className="top-actions">
        <ThemeToggle />
        <HeaderButtons/>
        <Profilebtn/>
      </div>
    </header>
  )
}
export default Header