// CSV 파일 제출
const handleSubmit = async () => {
  if (!selectedFile) {
    alert("CSV 파일을 선택해주세요.");
    return;
  }

  const formData = new FormData();
  formData.append("file", selectedFile);

  try {
    const response = await api.post(
      `/competitions/${competitionId}/submit`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

    alert("제출 완료!");
  } catch (err) {
    alert("제출 실패: " + err.response.data.error);
  }
};