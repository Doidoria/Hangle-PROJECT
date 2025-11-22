import '../css/leaderboard.scss'
import { useEffect, useState } from 'react';
import api from "../api/axiosConfig";
import { useSearchParams } from "react-router-dom"; //useSearchParams 변수 추가

const downloadCSV = async (saveId, fileName) => {
    try {
        const response = await api.get(`/api/competitions/csv/${saveId}/download2`, {
            responseType: "blob", // ★ 중요: 파일(Binary) 받기
        });

         const blob = new Blob([response.data], { type: "text/csv" });

        // Blob → 다운로드 URL 생성
        const url = window.URL.createObjectURL(blob);

        // 동적 <a> 태그 생성하여 다운로드 강제
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", fileName); // 파일명 지정
        document.body.appendChild(link);
        link.click();

        // 정리
        link.remove();
        window.URL.revokeObjectURL(url);

    } catch (error) {
        console.error("CSV 다운로드 실패:", error);
        alert("다운로드 실패");
    }
};

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

    //실제 화면 출력 데이터 추가 //변경
    const pagedData = pagedComp.map(comp => ({
        comp,
        entries: leaderboard.filter(entry => entry.competitionId === comp.id)
    }));

    // data 객체 재구성 추가
    const data = {
        content: pagedData,
        totalPages: Math.ceil(compItem.length / size),
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
        const fetchCompetitions = async () => {
            try {
                const resp = await api.get("/api/leaderboard")
                const data = resp.data;

                console.log("요청 URL:", resp);

                // 서버에서 받은 원본 저장
                const originalList = data.leaderboard || [];

                // 리더보드 원본은 별도 저장
                setLeaderboard(originalList);

                // compItem 생성
                const compItems = [
                    ...new Map(
                        originalList.map(item => [
                            item.competitionId,
                            { id: item.competitionId, title: item.competitionTitle }
                        ])
                    ).values()
                ];
                setCompItem(compItems);

                if (originalList.length === 0) {
                    setErrorMsg("검색 결과가 없습니다.");
                } else {
                    setErrorMsg("");
                }
            } catch (e) {
                console.error("리더보드 조회 오류", e)
                setErrorMsg("리더보드 정보를 불러오지 못했습니다")

            }

        };

        fetchCompetitions();
    }, []);


    useEffect(() => {
        if (!keyword.trim()) {
            // 검색어 없으면 원본 그대로 유지
            return;
        }

        setLeaderboard(prev =>
            prev.filter(item =>
                item.username.toLowerCase().includes(keyword.toLowerCase()) ||
                item.competitionTitle.toLowerCase().includes(keyword.toLowerCase())
            )
        );

    }, [keyword]);;



    // 대회별 그룹핑
    // const groupedByComp = compNameList.map((compName) => {
    //     const entries = leaderboard.filter((entry) => entry.competitionTitle === compName);
    //     return { compName, entries };
    // });


    return (
        <main className="main">
            <section className="section-wrap">
                <div>
                    <h1>
                        리더보드
                        {/* 추가 */}
                        <span class="material-symbols-outlined">crown</span>
                    </h1>
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
                                                <td>{(entry.score * 100).toFixed(2)} 점</td>
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
                                                {/* ✅ 다운로드 버튼 추가 */}
                                                <td>
                                                    <button
                                                        className="btn"
                                                        style={{
                                                            padding: "4px 8px",
                                                            fontSize: "12px",
                                                            background: "#10B981",
                                                            color: "#fff",
                                                            borderRadius: "4px",
                                                        }}
                                                        onClick={() => downloadCSV(entry.csvSave_id, entry.fileName)}
                                                    >
                                                        다운로드
                                                    </button>
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
                {/* <div style={{ marginTop: "1rem", background: "#f9f9f9", padding: "1rem" }}>
                    <h4>현재 상태 요약:</h4>
                    <ul>
                        <li>leaderboard 길이: {leaderboard.length}</li>
                        <li>compNameList: {compItem.join(", ") || "없음"}</li>
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

export default Leaderboard;