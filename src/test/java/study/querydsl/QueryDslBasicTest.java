package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {
    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() throws Exception{
        //when
        Member findByName = em.createQuery("select m from Member m " +
                        "where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        //then
        assertThat(findByName.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() throws Exception{
        // 직접 QMember 인스턴스를 생성해서 사용.
        // 이 경우는 같은 테이블을 join할 때, alias를 다르게 하기 위해 사용하는데... 거의 없다.
//        QMember m = new QMember("m");

        //when
        Member member1 = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() throws Exception{
        //given & when
        // 이런 식으로 and 연산자를 쉼표로 대체할 수 있다.
        // null도 허용하기 때문에 동적 쿼리를 만들 때 유용하다.
        Member member1 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10),
                        null // 동적 쿼리
                ).fetchOne();
        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() throws Exception{
        //given & when
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();
        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }
}
