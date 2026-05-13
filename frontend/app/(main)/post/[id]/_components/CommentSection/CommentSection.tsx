'use client';

import React, { useState } from 'react';
import styles from './CommentSection.module.css';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getComments, createComment, deleteComment, updateComment, Comment } from '@/lib/api/feedAPI';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import Link from 'next/link';
import DeleteConfirmModal from '../DeleteConfirmModal/DeleteConfirmModal';

interface Props {
  postId: string;
  onLoginRequired?: () => void;
}

export default function CommentSection({ postId, onLoginRequired }: Props) {
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuthStore();
  const [content, setContent] = useState('');
  const [replyingToId, setReplyingToId] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [deletingCommentId, setDeletingCommentId] = useState<string | null>(null);
  const [editingCommentId, setEditingCommentId] = useState<string | null>(null);
  const [editContent, setEditContent] = useState('');
  const [expandedReplies, setExpandedReplies] = useState<Record<string, boolean>>({});

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => getComments(postId, 0, 100),
  });

  const createMutation = useMutation({
    mutationFn: (data: { postId: string; content: string; parentId?: string }) => 
      createComment(data.postId, data.content, data.parentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] });
      setContent('');
      setReplyContent('');
      setReplyingToId(null);
      toast.success('댓글이 작성되었습니다.');
    },
    onError: (err: unknown) => {
      const error = err as { response?: { status?: number } };
      if (error?.response?.status === 401) {
        if (onLoginRequired) onLoginRequired();
        else toast.error('로그인이 필요합니다.');
      } else if (error?.response?.status === 422) {
        toast.error('댓글은 200자를 초과할 수 없습니다.');
      } else {
        toast.error('댓글 작성에 실패했습니다.');
      }
    }
  });

  const deleteMutation = useMutation({
    mutationFn: deleteComment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] });
      toast.success('댓글이 삭제되었습니다.');
    },
    onError: () => {
      toast.error('댓글 삭제에 실패했습니다.');
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, content }: { id: string; content: string }) => updateComment(id, content),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] });
      setEditingCommentId(null);
      setEditContent('');
      toast.success('댓글이 수정되었습니다.');
    },
    onError: () => {
      toast.error('댓글 수정에 실패했습니다.');
    }
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    if (!isAuthenticated) {
      if (onLoginRequired) onLoginRequired();
      else toast.error('댓글을 작성하려면 로그인이 필요합니다.');
      return;
    }
    createMutation.mutate({
      postId,
      content
    });
  };

  const handleReplySubmit = (parentId: string) => {
    if (!replyContent.trim()) return;
    if (!isAuthenticated) {
      if (onLoginRequired) onLoginRequired();
      else toast.error('로그인이 필요합니다.');
      return;
    }
    createMutation.mutate({
      postId,
      content: replyContent,
      parentId
    });
  };

  const handleDelete = (commentId: string) => {
    setDeletingCommentId(commentId);
  };

  const confirmDelete = () => {
    if (deletingCommentId) {
      deleteMutation.mutate(deletingCommentId);
      setDeletingCommentId(null);
    }
  };

  const handleEditSubmit = (commentId: string) => {
    if (!editContent.trim()) return;
    updateMutation.mutate({ id: commentId, content: editContent });
  };

  const comments = pageData?.content || [];

  const renderComment = (comment: Comment, isReply = false) => {
    if (!comment) return null;

    const isOwner = isAuthenticated && user && (
      String(user.id) === String(comment.userId) ||
      String(user.userId) === String(comment.userId)
    );
    const dateObj = new Date(comment.createdAt);
    const dateStr = `${dateObj.getFullYear()}.${String(dateObj.getMonth() + 1).padStart(2, '0')}.${String(dateObj.getDate()).padStart(2, '0')}`;

    // 삭제된 댓글: 작성자 정보 숨김 (유튜브 스타일)
    if (comment.isDeleted) {
      return (
        <div key={comment.commentId} className={isReply ? styles.replyItem : styles.commentItem}>
          <div className={styles.avatar} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', opacity: 0.4 }}>🧑</div>
          <div className={styles.commentContent}>
            <div className={styles.deletedText}>삭제된 댓글입니다.</div>
          </div>
        </div>
      );
    }

    return (
      <div key={comment.commentId} className={isReply ? styles.replyItem : styles.commentItem}>
        <Link href={`/profile/${comment.userId}`} style={{textDecoration: 'none', color: 'inherit'}}>
          {comment.profileImageUrl ? (
            <img src={comment.profileImageUrl} alt="아바타" className={styles.avatar} />
          ) : (
            <div className={styles.avatar} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>🧑</div>
          )}
        </Link>
        <div className={styles.commentContent}>
          <div className={styles.header}>
            <Link href={`/profile/${comment.userId}`} style={{textDecoration: 'none', color: 'inherit'}}>
              <span className={styles.nickname}>{comment.nickname}</span>
            </Link>
            <span className={styles.createdAt}>
              {dateStr} {comment.isEdited && <span className={styles.editedMark}>(수정됨)</span>}
            </span>
          </div>
          {editingCommentId === comment.commentId ? (
            <div className={styles.editWrapper}>
              <textarea
                className={styles.editTextarea}
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                maxLength={200}
                rows={2}
              />
              <div className={styles.editActions}>
                <button className={styles.cancelEditBtn} onClick={() => setEditingCommentId(null)}>취소</button>
                <button 
                  className={styles.saveEditBtn} 
                  onClick={() => handleEditSubmit(comment.commentId)}
                  disabled={updateMutation.isPending || !editContent.trim()}
                >
                  {updateMutation.isPending ? '저장중...' : '저장'}
                </button>
              </div>
            </div>
          ) : (
            <div className={styles.text}>{comment.content}</div>
          )}
          {editingCommentId !== comment.commentId && (
            <div className={styles.actions}>
              {!isReply && (
                <button 
                  className={styles.actionBtn}
                  onClick={() => setReplyingToId(comment.commentId)}
                >
                  답글 달기
                </button>
              )}
              {isOwner && (
                <>
                  <button 
                    className={styles.actionBtn} 
                    onClick={() => {
                      setEditingCommentId(comment.commentId);
                      setEditContent(comment.content);
                    }}
                  >
                    수정
                  </button>
                  <button className={styles.actionBtn} onClick={() => handleDelete(comment.commentId)}>삭제</button>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>댓글 {pageData?.totalElements || 0}</h3>
      
      {isLoading ? (
        <div style={{ padding: '20px', textAlign: 'center' }}>댓글 불러오는 중...</div>
      ) : (
        <div className={styles.commentList}>
          {comments.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#666', padding: '20px' }}>첫 번째 댓글을 남겨보세요!</div>
          ) : (
            comments.map(comment => (
              <React.Fragment key={comment.commentId}>
                {renderComment(comment)}
                {replyingToId === comment.commentId && (
                  <div className={styles.inlineReplyArea}>
                    <div className={styles.inlineReplyHeader}>
                      <span className={styles.inlineReplyLabel}>{comment.nickname}님에게 답글</span>
                      <button className={styles.inlineReplyCancelBtn} onClick={() => {
                        setReplyingToId(null);
                        setReplyContent('');
                      }}>✕</button>
                    </div>
                    <div className={styles.inputWrapper}>
                      <textarea
                        className={styles.textarea}
                        placeholder="답글을 입력하세요..."
                        value={replyContent}
                        onChange={(e) => setReplyContent(e.target.value)}
                        maxLength={200}
                        rows={1}
                        autoFocus
                        onInput={(e) => {
                          const target = e.target as HTMLTextAreaElement;
                          target.style.height = 'auto';
                          target.style.height = `${target.scrollHeight}px`;
                        }}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' && !e.shiftKey) {
                            if (e.nativeEvent.isComposing) return;
                            e.preventDefault();
                            handleReplySubmit(comment.commentId);
                          }
                        }}
                      />
                      <button 
                        type="button" 
                        className={styles.submitBtn}
                        disabled={!replyContent.trim() || createMutation.isPending}
                        onClick={() => handleReplySubmit(comment.commentId)}
                      >
                        {createMutation.isPending ? '...' : '↑'}
                      </button>
                    </div>
                  </div>
                )}
                {comment.replies && comment.replies.length > 0 && (
                  <div className={styles.repliesToggleWrapper}>
                    <button 
                      className={styles.repliesToggleBtn}
                      onClick={() => setExpandedReplies(prev => ({ ...prev, [comment.commentId]: !prev[comment.commentId] }))}
                    >
                      {expandedReplies[comment.commentId] ? '▲ 답글 숨기기' : `▼ 답글 ${comment.replies.length}개 보기`}
                    </button>
                  </div>
                )}
                {expandedReplies[comment.commentId] && comment.replies?.map(reply => renderComment(reply, true))}
              </React.Fragment>
            ))
          )}
        </div>
      )}

      <form className={styles.inputWrapper} onSubmit={handleSubmit}>
        <textarea
          className={styles.textarea}
          placeholder={isAuthenticated ? "댓글을 입력하세요..." : "로그인 후 댓글을 남겨보세요."}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={200}
          rows={1}
          onInput={(e) => {
            const target = e.target as HTMLTextAreaElement;
            target.style.height = 'auto';
            target.style.height = `${target.scrollHeight}px`;
          }}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              if (e.nativeEvent.isComposing) return;
              e.preventDefault();
              handleSubmit(e);
            }
          }}
        />
        <button 
          type="submit" 
          className={styles.submitBtn}
          disabled={!content.trim() || createMutation.isPending}
        >
          {createMutation.isPending ? '...' : '↑'}
        </button>
      </form>

      <DeleteConfirmModal
        isOpen={deletingCommentId !== null}
        title="댓글을 삭제하시겠습니까?"
        description="삭제된 댓글은 복구할 수 없습니다."
        onConfirm={confirmDelete}
        onCancel={() => setDeletingCommentId(null)}
      />
    </div>
  );
}
