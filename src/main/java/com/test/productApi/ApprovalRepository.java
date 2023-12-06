package com.test.productApi;
        import jakarta.transaction.Transactional;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Modifying;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.stereotype.Repository;

        import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalQueue, Integer> {

        @Query(value="select * from approval_queue order by approval_request_date",nativeQuery = true)
        public List<ApprovalQueue> getApprovalsorderedbydate();


        @Query(value="select * from approval_queue where approvalid= :approvalId",nativeQuery = true)
        public ApprovalQueue getApprovalItemById(int approvalId);


        @Transactional
        @Modifying
        @Query(value="Delete from approval_queue where approvalid= :approvalId",nativeQuery = true)
        public int removeApprovalItembyId(int approvalId);
}