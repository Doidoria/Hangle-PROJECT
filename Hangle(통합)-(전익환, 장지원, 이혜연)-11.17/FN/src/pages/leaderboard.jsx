import Layout from './Layout.jsx'
import '../css/leaderboard.scss'
import { useEffect, useState } from 'react';
import { useSearchParams } from "react-router-dom"; //useSearchParams 변수 추가


const Leaderboard = () => {

    const [leaderboard, setLeaderboard] = useState([]);
    const [compItem, setCompItem] = useState([]); //변경
    const [keyword, setKeyword] = useState("");
    // isempty삭제 처리
    const [errorMsg, setErrorMsg] = useState('');

    //페이징 처리하기 위한 변수 생성
    const [searchParams, setSearchParams] = useSearchParams(); //useSearchParams 변수 추가
    const page = Number(searchParams.get('page') || 0); //page변수 추가
    const size = Number(searchParams.get('size') || 2); //size 변수 추가

    //전체 대회 목록 추가 //삭제
    // const compNames = [...new Set(leaderboard.map(item => item.competitionTitle))];  
    
    const compStart = page * size;
    const compEnd = compStart + size;

    //페이지에 해당하는 pagedComp 추가 
    const pagedComp = compItem.slice(compStart, compEnd);
    
    //실제 화면 출력 데이터 추가 
    const pagedData = pagedComp.map(comp => ({
        comp,
        entries: leaderboard.filter(entry => entry.competitionTitle === compName)
    }));
    
    // data 객체 재구성 추가
    const data = {
        content: pagedData,
        totalPages: Math.ceil(compNames.length / size),
        page: page
    };


    const onSearch = (e) => {
        e.preventDefault();
        const form = new FormData(e.currentTarget);
        const newkeyword = form.get("keyword") || "";
        setKeyword(newkeyword);
        
        //페이징처리추가
        const next = new URLSearchParams(searchParams);
        next.set('page', '0'); // 새 검색은 첫 페이지부터
        next.set('size', String(size));
        setSearchParams(next);
    };

    //페이지 함수 추가
    const movePage = (nextPage) => {
        const next = new URLSearchParams(searchParams);
        next.set('page', String(nextPage));
        setSearchParams(next);
    };

    useEffect(() => {
        fetch("http://localhost:8090/api/v1/leaderboard")
            .then((res) => res.json())
            .then((data) => {
                let list = data.leaderboard || [];

                //서버의 엠티값 읽기 -> 삭제처리

                //키워드 비어있음 -> 전체 조회
                if (keyword.trim() !== "") {
                    list = list.filter(
                        (item) =>
                            item.username.toLowerCase().includes(keyword.toLowerCase()) ||
                            item.competitionTitle.toLowerCase().includes(keyword.toLowerCase())
                    );
                }

                setLeaderboard(list);

                // 추가 + 변경
                const filteredCompItem = [...new Map(
                    list.map(item => [item.competitionId, { id: item.competitionId, title: item.competitionTitle }])
                ).values()];
                setCompItem(filteredCompItem);

            })
            .then((data) => { console.log("data : ", data) })
            .catch((err) => console.error(err));
    }, [keyword]);


    //추가
       useEffect(() => {
        if (compItem.length === 0) {
            setErrorMsg("검색 결과가 없습니다.");
        } else {
            setErrorMsg("");
        }
    }, [compItem]);


    return (
        <main className="main">
            <section className="section-wrap">
                <div>
                    <h1>리더보드 🏆</h1>
                    <p>상위권 참가자의 점수를 확인하세요.</p>
                </div>

                <form className="search" onSubmit={onSearch}>
                    <input name="keyword" placeholder="검색어" />
                    <button className="btn" type="submit">검색</button>
                </form>

                <div>
                    {/* 에러메세지 추가 */}
                    {errorMsg && <div style={{ color: '#c00', marginBottom: 8 }}>{errorMsg}</div>}


                    {/* {groupedByComp대신 data로 바꿈 (밑의 세줄)*/}
                     {data.content.map(({ comp, entries }) => (
                        <div key={comp.id}>
                            <h3>{comp.title}</h3>
                            <div className="card" style={{ overflowX: "auto" }}>
                                <table className="leaderboard" style={{ width: "100%", borderCollapse: "collapse" }}>
                                    <thead>
                                        <tr>
                                            <th>순위</th>
                                            <th>닉네임</th>
                                            <th>점수</th>
                                            <th>제출 횟수</th>
                                            <th>최근 제출일</th>
                                        </tr>
                                    </thead>
                                    <tbody className="leaderboardBody">
                                        {entries.map((entry) => (
                                            <tr key={entry.leaderBoardId}>
                                                <td>{entry.comprank}</td>
                                                <td>{entry.username}</td>
                                                <td>{entry.score}</td>
                                                <td>{entry.attempt}</td>
                                                <td>
                                                    {new Date(entry.submittedAt).toLocaleString("ko-KR", {
                                                        year: "numeric",
                                                        month: "2-digit",
                                                        day: "2-digit",
                                                        hour: "2-digit",
                                                        minute: "2-digit",
                                                    })}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    ))}

                </div>


                {/* 페이지네이션 추가*/}
                <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginTop: 12 }}>
                    <button disabled={page <= 0} onClick={() => movePage(page - 1)}>이전</button>
                    <span>Page {page + 1} / {Math.max(data.totalPages, 1)}</span>
                    <button disabled={page + 1 >= data.totalPages} onClick={() => movePage(page + 1)}>다음</button>
                </div>



                {/* 데이터 받아오는 거 확인 (기본)*/}
                {/* isempty 삭제 처리 */}
                {/* <div style={{ marginTop: "1rem", background: "#f9f9f9", padding: "1rem" }}>
                    <h4>현재 상태 요약:</h4>
                    <ul>
                        <li>leaderboard 길이: {leaderboard.length}</li>
                        <li>compNameList: {compNameList.join(", ") || "없음"}</li>
                        <li>keyword: {keyword || "없음"}</li>
                    </ul>
                </div>

                <pre style={{ background: "#eee", padding: "1rem", borderRadius: "8px" }}>
                    {JSON.stringify(leaderboard, null, 2)}
                </pre> */}

            </section>
        </main>
    )
}

export default Leaderboard