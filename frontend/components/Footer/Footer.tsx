import Link from 'next/link';
import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.footerInner}>
        <div className={styles.footerTop}>
          <div className={styles.footerBrand}>
            <div className={styles.footerLogo}>
              <span className={styles.footerLogoIcon}>S</span>
              <span className={styles.footerLogoText}>SIMUL</span>
            </div>
            <p className={styles.footerDesc}>
              AI 가상시착으로 나만의 스타일을 발견하세요.
              패션을 탐색하고, 시착하고, 공유하는 새로운 경험.
            </p>
          </div>

          <div className={styles.footerLinks}>
            <div className={styles.footerLinkGroup}>
              <h4>서비스</h4>
              <ul>
                <li><Link href="/tryon">가상시착</Link></li>
                <li><Link href="/closet">내 옷장</Link></li>
                <li><Link href="/">커뮤니티</Link></li>
              </ul>
            </div>
            <div className={styles.footerLinkGroup}>
              <h4>지원</h4>
              <ul>
                <li><Link href="#">고객센터</Link></li>
                <li><Link href="#">이용약관</Link></li>
                <li><Link href="#">개인정보처리방침</Link></li>
              </ul>
            </div>
            <div className={styles.footerLinkGroup}>
              <h4>회사</h4>
              <ul>
                <li><Link href="#">소개</Link></li>
                <li><Link href="#">채용</Link></li>
                <li><Link href="#">문의</Link></li>
              </ul>
            </div>
          </div>
        </div>

        <div className={styles.footerBottom}>
          <span className={styles.footerCopyright}>
            © 2026 SIMUL. All rights reserved.
          </span>
        </div>
      </div>
    </footer>
  );
}
