package kidlike;

import static java.util.Optional.ofNullable;
import static kidlike.viber.schema.Tables.CONTACT;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.TelephoneType;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import kidlike.viber.schema.tables.records.ContactRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "viber-to-vcard",
    description = "Extract contacts from a Viber datafile, to vCard format",
    version = "viber-to-vcard 1.0",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true)
public class ViberToVcardCommand implements Runnable {

    @Parameters(
        index = "0",

        description = "The path of the Viber datafile - usually resides in ${HOME}/.ViberPC/<yourPhoneNumber>/viber.db")
    String viberDatafilePath;

    @Option(
        names = {"-o", "--out"},
        arity = "0..1",
        description = "Optional path of the output file, in vCard format. unset = STDOUT")
    String outputPath;

    public void run() {
        System.err.println("# Parsing " + viberDatafilePath);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + viberDatafilePath, sqLiteProperties())) {
            DSLContext dsl = DSL.using(conn, SQLDialect.SQLITE);
            List<ContactRecord> contacts = dsl.select().from(CONTACT).fetchInto(ContactRecord.class);

            Map<String, List<String>> nameWithNumbers = mapContactsByName(contacts);
            List<VCard> vCards = mapToVCard(nameWithNumbers);
            printVcards(vCards);

            System.err.println("# Done!");
            ofNullable(outputPath).ifPresent(path -> System.err.println("# Output file: " + path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The filtering on {@link ContactRecord#getClientname()}, is done because it seems that Viber is using it to store "deprecated"
     * contacts (for example previous phone-number owner?).
     */
    private static Map<String, List<String>> mapContactsByName(List<ContactRecord> contacts) {
        return contacts.stream()
            .filter(contact -> contact.getClientname() == null)
            .filter(contact -> contact.getName() != null)
            .filter(contact -> contact.getNumber() != null)
            .collect(Collectors.toMap(ContactRecord::getName,
                contact -> List.of(contact.getNumber()),
                (strings, strings2) -> {
                    var ret = new ArrayList<String>(strings.size() + strings2.size());
                    ret.addAll(strings);
                    ret.addAll(strings2);
                    return ret;
                }));
    }

    private static List<VCard> mapToVCard(Map<String, List<String>> nameWithNumbers) {
        return nameWithNumbers.entrySet().stream()
            .map(e -> {
                VCard vcard = new VCard();
                vcard.setFormattedName(e.getKey());

                e.getValue().forEach(number -> {
                    TelephoneType type = TelephoneType.CELL;
                    if (number.startsWith("+302")) {
                        type = TelephoneType.HOME;
                    }
                    vcard.addTelephoneNumber(number, type);
                });

                return vcard;
            })
            .toList();
    }

    private void printVcards(List<VCard> vCards) throws IOException {
        Ezvcard.write(vCards)
            .version(VCardVersion.V4_0)
            .go(ofNullable(outputPath).map(path -> {
                try {
                    return (OutputStream) new FileOutputStream(path);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            }).orElse(System.out));
    }

    private static Properties sqLiteProperties() {
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.setReadOnly(true);
        return sqLiteConfig.toProperties();
    }

    public static void main(String[] args) {
        new CommandLine(new ViberToVcardCommand()).execute(args);
    }
}
