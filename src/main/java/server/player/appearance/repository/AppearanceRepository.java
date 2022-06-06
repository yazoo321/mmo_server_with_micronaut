package server.player.appearance.repository;

import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import io.reactivex.Single;
import server.configuration.PlayerCharacterConfiguration;
import server.player.appearance.model.AppearancePiece;

import javax.inject.Singleton;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Singleton
public class AppearanceRepository {

    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<AppearancePiece> appearancePieceCollection;

    public AppearanceRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Boolean savePieces(List<AppearancePiece> pieceList) {
        InsertManyResult res = Flowable.fromPublisher(
                appearancePieceCollection.insertMany(pieceList)
        ).blockingFirst();

        return res.wasAcknowledged();
    }

    public Boolean savePiece(AppearancePiece piece) {
        InsertOneResult res = Single.fromPublisher(
                appearancePieceCollection.insertOne(piece)
        ).blockingGet();

        return res.wasAcknowledged();
    }

    public List<AppearancePiece> fetchPiecesByRaceGenderItemGroup(String race, Boolean isMale, String itemGroup) {
        return Flowable.fromPublisher(
                appearancePieceCollection.find(
                        and(
                                eq("race", race),
                                and(
                                        eq("isMale", isMale),
                                        and(
                                                eq("itemGroup", itemGroup)
                                        )
                                )

                        )

                )
        ).toList().blockingGet();
    }

    public List<AppearancePiece> getDefaultAppearanceFor(String race, Boolean isMale) {
        return Flowable.fromPublisher(
                appearancePieceCollection.find(
                        and(
                                eq("race", race),
                                and(
                                        eq("isMale", isMale),
                                        and(
                                                eq("isDefault", true)
                                        )
                                )

                        )

                )
        ).toList().blockingGet();
    }

    public List<AppearancePiece> getBaseAppearanceUsing(String race, Boolean isMale, String part) {
        return Flowable.fromPublisher(
                appearancePieceCollection.find(
                        and(eq("race", race),
                                and(eq("isMale", isMale),
                                        and(eq("part", part),
                                                and(eq("isBase", true)))))
                )
        ).toList().blockingGet();
    }

    public AppearancePiece findByPartMeshMaterial(String part, String mesh, String material) {
        // this should be a unique combination similar to ID
        try {
            return Single.fromPublisher(
                    appearancePieceCollection.find(
                            and(
                                    eq("part", part),
                                    and(
                                            eq("mesh", mesh),
                                            and(
                                                    eq("material", material)
                                            )
                                    )
                            )
                    )
            ).blockingGet();
        } catch(NoSuchElementException e) {
            return null;
        }

    }

    private void prepareCollections() {
        this.appearancePieceCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), AppearancePiece.class);
    }
}
