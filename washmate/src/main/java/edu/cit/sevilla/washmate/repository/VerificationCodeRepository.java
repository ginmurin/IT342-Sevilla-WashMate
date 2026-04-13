package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Find active (unexpired and unused) verification code for user
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.userId = :userId AND vc.codeType = :codeType AND vc.isUsed = false AND vc.expiresAt > CURRENT_TIMESTAMP")
    Optional<VerificationCode> findActiveByUserIdAndType(@Param("userId") Long userId, @Param("codeType") String codeType);

    /**
     * Find any verification code for user (expired or not)
     */
    Optional<VerificationCode> findByUserIdAndCodeType(Long userId, String codeType);

    /**
     * Find by code and user ID
     */
    Optional<VerificationCode> findByUserIdAndCode(Long userId, String code);

    /**
     * Delete expired codes (scheduled task)
     */
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt <= CURRENT_TIMESTAMP")
    void deleteExpiredCodes();

    /**
     * Find all expired or used codes for cleanup
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.expiresAt <= :expiredBefore OR vc.isUsed = true")
    List<VerificationCode> findExpiredOrUsedCodes(@Param("expiredBefore") LocalDateTime expiredBefore);
}
