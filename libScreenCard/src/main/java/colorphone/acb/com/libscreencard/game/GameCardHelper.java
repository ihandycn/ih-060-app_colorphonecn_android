package colorphone.acb.com.libscreencard.game;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.h5game.AcbH5GameInfo;

import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libscreencard.CardCustomConfig;
import colorphone.acb.com.libscreencard.R;
import colorphone.acb.com.libscreencard.gif.AutoPilotUtils;

/**
 * Created by sundxing on 2018/6/20.
 */

public class GameCardHelper {

    public static View getFourInOneView(Context context) {
        View gameCard = View.inflate(context, R.layout.sc_layout_card_game_four, null);
        gameCard.findViewById(R.id.container_view).setBackgroundDrawable(
                BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#44ecf3fd"), Dimensions.pxFromDp(8), false));

        List<ImageView> views = new ArrayList<>(4);
        views.add(gameCard.findViewById(R.id.card_img_top_left));
        views.add(gameCard.findViewById(R.id.card_img_top_right));
        views.add(gameCard.findViewById(R.id.card_img_bottom_left));
        views.add(gameCard.findViewById(R.id.card_img_bottom_right));

        boolean gamesReady = GameManager.getInstance().isRandomGamesReady();
        if (gamesReady) {
            final List<AcbH5GameInfo> gameInfos = GameManager.getInstance().getRandomGames();
            for (int i = 0; i < 4; i++) {
                final AcbH5GameInfo gameInfo = gameInfos.get(i);
                ImageView gameItem = views.get(i);
                gameItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GameManager.getInstance().startGame(gameInfo);
                    }
                });
                Glide.with(context).asBitmap()
                        .load(gameInfo.getLargePictureURL())
                        .into(gameItem);

            }
        } else {
            GameManager.getInstance().prepareRandomGames();
        }

        return gameCard;
    }

    public static View getOneCardGameView(Context context, Runnable dismissRunnable, boolean fmGame) {
        AcbH5GameInfo gameInfo = null;
        if (fmGame) {
            List<AcbH5GameInfo> randomGames = GameManager.getInstance().getRandomGames();
            if (randomGames.isEmpty()) {
                GameManager.getInstance().prepareRandomGames();
                return null;
            }
            gameInfo = randomGames.get(0);
        }
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Game_Card_Clicked");
                if (fmGame) {
                    AutoPilotUtils.logFmCardClick();
                } else {
                    AutoPilotUtils.gameClick();
                }
                if (dismissRunnable != null) {
                    dismissRunnable.run();
                }
                GameManager.getInstance().startGame();
            }
        };

        View gameCard = View.inflate(context, R.layout.sc_layout_card_game_issue_custom, null);
        ImageView imageView = gameCard.findViewById(R.id.card_img_top_left);
        TextView titleTv = gameCard.findViewById(R.id.security_protection_card_game_issue_title);
        TextView subTitleTv = gameCard.findViewById(R.id.security_protection_card_game_issue_subtitle);

        if (fmGame) {
            Glide.with(context).asBitmap()
                    .load(gameInfo.getLargePictureURL())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.game_card_bg_basketball);
        }
        // Use local for test.
        titleTv.setText(fmGame ? gameInfo.getTitle() : context.getString(R.string.game_card_title));
        subTitleTv.setText(fmGame ? gameInfo.getShortDescription() : context.getString(R.string.game_card_desc));
        View playButton = gameCard.findViewById(R.id.security_protection_game_issue_btn);
        playButton.setOnClickListener(clickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playButton.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(new Rect(0, (int) (0.1f * view.getHeight()),view.getWidth(),view.getHeight()), view.getHeight() / 2);
                }
            });
        }

        gameCard.findViewById(R.id.container_view).setOnClickListener(clickListener);
        if (fmGame) {
            AutoPilotUtils.gameShow();
        } else {
            AutoPilotUtils.logFmCardShow();
        }
        CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Game_Card_Show");
        return gameCard;
    }
}
