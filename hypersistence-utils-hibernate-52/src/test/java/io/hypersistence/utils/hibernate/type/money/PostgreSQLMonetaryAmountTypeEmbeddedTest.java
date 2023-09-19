package io.hypersistence.utils.hibernate.type.money;

import io.hypersistence.utils.hibernate.util.AbstractPostgreSQLIntegrationTest;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.MonetaryAmount;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Nikola Malenic
 */
public class PostgreSQLMonetaryAmountTypeEmbeddedTest extends AbstractPostgreSQLIntegrationTest {
    @Override
    protected Class<?>[] entities() {
        return new Class[]{
            Salary.class
        };
    }

    @Test
    public void testPersistAndReadMoney() {
        Salary _salary = doInJPA(entityManager -> {
            Salary salary = new Salary();
            salary.setEmbeddedSalary(new EmbeddableMonetaryAmount(Money.of(new BigDecimal("10.23"), "USD")));

            entityManager.persist(salary);

            return salary;
        });

        doInJPA(entityManager -> {
            Salary salary = entityManager.find(Salary.class, _salary.getId());

            assertEquals(salary.getEmbeddedSalary().amount, Money.of(new BigDecimal("10.23"), "USD"));
        });
    }

    @Test
    public void testSearchByMoney() {
        doInJPA(entityManager -> {
            Salary salary1 = new Salary();
            salary1.setEmbeddedSalary(new EmbeddableMonetaryAmount(Money.of(new BigDecimal("10.23"), "USD")));
            entityManager.persist(salary1);

            Salary salary2 = new Salary();
            salary2.setEmbeddedSalary(new EmbeddableMonetaryAmount((Money.of(new BigDecimal("20.23"), "EUR"))));
            entityManager.persist(salary2);
        });

        doInJPA(entityManager -> {
            Money money = Money.of(new BigDecimal("10.23"), "USD");
            Salary salary = entityManager.createQuery("select s from Salary s where s" +
                                                      ".embeddedSalary.amount = :amount", Salary.class)
                .setParameter("amount", money)
                .getSingleResult();

            assertEquals(1, salary.getId());
        });
    }

    @Test
    public void testSearchByComponents() {
        doInJPA(entityManager -> {
            Salary salary1 = new Salary();
            salary1.setEmbeddedSalary(new EmbeddableMonetaryAmount((Money.of(new BigDecimal("10.23"), "USD"))));
            entityManager.persist(salary1);

            Salary salary2 = new Salary();
            salary2.setEmbeddedSalary(new EmbeddableMonetaryAmount((Money.of(new BigDecimal("20.23"), "EUR"))));
            entityManager.persist(salary2);
        });

        doInJPA(entityManager -> {
            BigDecimal amount = BigDecimal.TEN;
            List<Salary> salaries = entityManager.createQuery("select s from Salary s where s.embeddedSalary.amount.amount >= :amount", Salary.class)
                .setParameter("amount", amount)
                .getResultList();


            assertEquals(1L, salaries.get(0).getId());
            assertEquals(2L, salaries.get(1).getId());
        });

        doInJPA(entityManager -> {
            String currency = "USD";
            Salary salary = entityManager.createQuery("select s from Salary s where s.embeddedSalary.amount.currency = :currency", Salary.class)
                .setParameter("currency", currency)
                .getSingleResult();

            assertEquals(1L, salary.getId());
        });
    }

    @Entity(name = "Salary")
    @Table(name = "salary")
    public static class Salary {
        @Id
        @GeneratedValue
        private long id;

        private String other;

        @Embedded
        private EmbeddableMonetaryAmount embeddedSalary;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public EmbeddableMonetaryAmount getEmbeddedSalary() {
            return embeddedSalary;
        }

        public void setEmbeddedSalary(EmbeddableMonetaryAmount embeddedSalary) {
            this.embeddedSalary = embeddedSalary;
        }

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }
    }

    @Embeddable
    @TypeDef(name = "monetary-amount-currency", typeClass = MonetaryAmountType.class, defaultForType = MonetaryAmount.class)
    public static class EmbeddableMonetaryAmount {
        @Columns(columns = {
            @Column(name = "salary_amount"),
            @Column(name = "salary_currency")
        })
        @Type(type = "monetary-amount-currency")
        private MonetaryAmount amount;

        public EmbeddableMonetaryAmount(MonetaryAmount amount) {
            this.amount = amount;
        }

        public EmbeddableMonetaryAmount() {

        }


        public MonetaryAmount getAmount() {
            return amount;
        }

        public void setAmount(MonetaryAmount amount) {
            this.amount = amount;
        }
    }
}
