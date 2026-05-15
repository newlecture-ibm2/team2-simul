'use client';

import { useState } from 'react';
import styles from './ReportManagementSection.module.css';
import { useReportManagement } from './useReportManagement';
import ConfirmModal from '../ConfirmModal';
import Switch from '../Switch';

export default function ReportManagementSection() {
  const { reports, isLoading, blindPost, unblindPost, suspendUser } = useReportManagement();
  
  const [modalConfig, setModalConfig] = useState<{
    isOpen: boolean;
    title: string;
    description: string;
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    description: '',
    onConfirm: () => {},
  });

  const openConfirm = (title: string, description: string, onConfirm: () => void) => {
    setModalConfig({ isOpen: true, title, description, onConfirm });
  };

  const closeConfirm = () => {
    setModalConfig((prev) => ({ ...prev, isOpen: false }));
  };

  if (isLoading) {
    return <div className={styles.loading}>데이터를 불러오는 중입니다...</div>;
  }

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>신고 관리</h2>
      
      {reports.length === 0 ? (
        <div className={styles.empty}>신고 내역이 없습니다.</div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>게시물 ID</th>
                <th>신고자 ID</th>
                <th>신고 사유</th>
                <th>신고 일시</th>
                <th>블라인드 여부</th>
                <th>계정 정지</th>
              </tr>
            </thead>
            <tbody>
              {reports.map((report) => (
                <tr key={report.reportId}>
                  <td className={styles.cellId} data-label="게시물 ID">{report.postId}</td>
                  <td className={styles.cellId} data-label="신고자 ID">{report.reporterId}</td>
                  <td data-label="신고 사유">{report.reason}</td>
                  <td data-label="신고 일시">{new Date(report.createdAt).toLocaleString()}</td>
                  <td data-label="블라인드 여부">
                    <div className={styles.actionGroup}>
                      <Switch
                        checked={report.isBlinded}
                        onChange={(checked) => {
                          if (checked) {
                            openConfirm('게시물 숨김', '해당 게시물을 블라인드 처리하시겠습니까?', () => {
                              blindPost(report.postId);
                              closeConfirm();
                            });
                          } else {
                            openConfirm('블라인드 해제', '해당 게시물의 블라인드를 해제하시겠습니까?', () => {
                              unblindPost(report.postId);
                              closeConfirm();
                            });
                          }
                        }}
                      />
                    </div>
                  </td>
                  <td data-label="계정 정지">
                      <button 
                        className={styles.btnDanger}
                        onClick={() => openConfirm('유저 정지', '해당 피신고자를 정지 처리하시겠습니까?', () => {
                          const targetUserId = report.reportedUserId || report.reporterId;
                          suspendUser(targetUserId); 
                          closeConfirm();
                        })}
                      >
                        유저 정지
                      </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmModal 
        isOpen={modalConfig.isOpen}
        title={modalConfig.title}
        description={modalConfig.description}
        onConfirm={modalConfig.onConfirm}
        onCancel={closeConfirm}
      />
    </div>
  );
}
