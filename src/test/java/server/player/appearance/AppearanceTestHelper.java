package server.player.appearance;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import server.configuration.PlayerCharacterConfiguration;
import server.player.appearance.model.AppearancePiece;
import server.player.appearance.repository.AppearanceRepository;
import server.player.appearance.utils.Part;
import server.player.appearance.utils.Race;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

@Singleton
public class AppearanceTestHelper {

    @Inject
    AppearanceRepository appearanceRepository;

    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<AppearancePiece> appearanceCollection;


    public AppearanceTestHelper(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public static AppearancePiece createBasicMaleHeadPiece() {
        AppearancePiece piece = createBasePiece();

        piece.setPart(Part.HEAD.value);
        piece.setMesh("M_HEAD_MESH");
        piece.setMaterial("M_HEAD_MATERIAL_NAME");
        piece.setRace(Race.HUMAN.value);
        piece.setItemGroup("SKIN_COLOR_1");

        return piece;
    }

    private static AppearancePiece createBasePiece() {
        AppearancePiece piece = new AppearancePiece();
        piece.setId(UUID.randomUUID().toString());
        piece.setIsBase(true);
        piece.setIsDefault(true);
        piece.setIsMale(true);

        return piece;
    }

    public static AppearancePiece createBasicMaleHairPiece() {
        AppearancePiece piece = createBasePiece();

        piece.setPart(Part.HAIR.value);
        piece.setMesh("M_HAIR_MESH");
        piece.setMaterial("M_HAIR_MATERIAL_NAME");
        piece.setRace(Race.HUMAN.value);
        piece.setItemGroup("HAIR_COLOR_1");

        return piece;
    }

    public static AppearancePiece createBasicMaleChestPiece() {
        AppearancePiece piece = createBasePiece();

        piece.setPart(Part.CHEST.value);
        piece.setMesh("M_CHEST_MESH");
        piece.setMaterial("M_CHEST_MATERIAL_NAME");
        piece.setRace(Race.HUMAN.value);
        piece.setItemGroup("SKIN_COLOR_1");

        return piece;
    }

    public static AppearancePiece createBasicFemaleHeadPiece() {
        AppearancePiece piece = createBasePiece();

        piece.setId(UUID.randomUUID().toString());
        piece.setPart(Part.HAIR.value);
        piece.setMesh("F_HEAD_MESH");
        piece.setMaterial("F_HEAD_MATERIAL_NAME");
        piece.setIsMale(false);
        piece.setRace(Race.HUMAN.value);
        piece.setItemGroup("SKIN_COLOR_1");

        return piece;
    }

    public void clearAllAppearanceData() {
        Flowable.fromPublisher(
                appearanceCollection.deleteMany(
                        ne("id", "delete all")
                )
        ).blockingFirst();
    }

    public List<AppearancePiece> getAllBasePieces() {
        return Flowable.fromPublisher(
                appearanceCollection.find(
                        eq("isBase", true)
                )
        ).toList().blockingGet();
    }

    private void prepareCollections() {
        this.appearanceCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), AppearancePiece.class);
    }

}
