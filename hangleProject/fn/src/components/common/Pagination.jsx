@use '../base/tokens' as *;
@use './_buttons';

.pagination {
  display: flex; gap: 8px; align-items: center;
  button { @extend .btn; }
  .page-info { color: $muted; font-size: 13px; }
}
