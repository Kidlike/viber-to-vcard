package kidlike;

import static kidlike.viber.schema.Tables.CONTACT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kidlike.viber.schema.tables.Contact;
import kidlike.viber.schema.tables.records.ContactRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "viber-to-vcard", description = "...",
        mixinStandardHelpOptions = true)
public class ViberToVcardCommand implements Runnable {

    @Parameters(index = "0")
    String viberDataFilePath;

    public static void main(String[] args) {
        new CommandLine(new ViberToVcardCommand()).execute(args);
    }

    public void run() {
        String url = "jdbc:sqlite:" + viberDataFilePath;

        try (Connection conn = DriverManager.getConnection(url, new Properties())) {

            DSLContext dsl = DSL.using(conn, SQLDialect.SQLITE);

            List<ContactRecord> contacts = dsl.select().from(CONTACT).fetchInto(ContactRecord.class);

            System.out.println(contacts.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
