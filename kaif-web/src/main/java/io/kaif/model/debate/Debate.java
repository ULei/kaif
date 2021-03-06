package io.kaif.model.debate;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.kaif.flake.FlakeId;
import io.kaif.kmark.KmarkProcessor;
import io.kaif.model.account.Account;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
import io.kaif.web.v1.dto.V1DebateDto;

public class Debate {

  public static final FlakeId NO_PARENT = FlakeId.MIN;
  private static final int MAX_LEVEL = 10;
  public static final int CONTENT_MIN = 5;
  public static final int CONTENT_MAX = 4096;

  public static Debate create(Article article,
      FlakeId debateId,
      @Nullable Debate parent,
      String content,
      Account debater,
      Instant now) {
    Optional<Debate> optParent = Optional.ofNullable(parent);
    FlakeId parentId = optParent.map(Debate::getDebateId).orElse(NO_PARENT);
    int parentLevel = optParent.map(Debate::getLevel).orElse(0);
    UUID replyToAccountId = optParent.map(Debate::getDebaterId).orElseGet(article::getAuthorId);
    return new Debate(article.getArticleId(),
        debateId,
        article.getZone(),
        parentId,
        replyToAccountId,
        parentLevel + 1,
        content,
        DebateContentType.MARK_DOWN,
        debater.getAccountId(),
        debater.getUsername(),
        0,
        0,
        now,
        now);
  }

  public static String renderContentPreview(String rawContent) {
    return KmarkProcessor.process(rawContent);
  }

  private final FlakeId articleId;
  private final FlakeId debateId;
  private final Zone zone;
  private final FlakeId parentDebateId;
  private final int level;
  private final String content;
  private final DebateContentType contentType;
  private final UUID debaterId;
  private final String debaterName;
  private final long upVote;
  private final long downVote;
  private final Instant createTime;
  private final Instant lastUpdateTime;
  private final UUID replyToAccountId;

  Debate(FlakeId articleId,
      FlakeId debateId,
      Zone zone,
      FlakeId parentDebateId,
      UUID replyToAccountId,
      int level,
      String content,
      DebateContentType contentType,
      UUID debaterId,
      String debaterName,
      long upVote,
      long downVote,
      Instant createTime,
      Instant lastUpdateTime) {
    Preconditions.checkArgument(level <= MAX_LEVEL);
    Preconditions.checkArgument(!NO_PARENT.equals(debateId));
    this.zone = zone;
    this.articleId = articleId;
    this.debateId = debateId;
    this.parentDebateId = parentDebateId;
    this.replyToAccountId = replyToAccountId;
    this.level = level;
    this.content = content;
    this.contentType = contentType;
    this.debaterId = debaterId;
    this.debaterName = debaterName;
    this.upVote = upVote;
    this.downVote = downVote;
    this.createTime = createTime;
    this.lastUpdateTime = lastUpdateTime;
  }

  public FlakeId getArticleId() {
    return articleId;
  }

  public FlakeId getDebateId() {
    return debateId;
  }

  public FlakeId getParentDebateId() {
    return parentDebateId;
  }

  public boolean hasParent() {
    return !parentDebateId.equals(NO_PARENT);
  }

  public int getLevel() {
    return level;
  }

  public String getContent() {
    return content;
  }

  public DebateContentType getContentType() {
    return contentType;
  }

  public UUID getDebaterId() {
    return debaterId;
  }

  public String getDebaterName() {
    return debaterName;
  }

  public long getUpVote() {
    return upVote;
  }

  public long getDownVote() {
    return downVote;
  }

  public Instant getCreateTime() {
    return createTime;
  }

  public Instant getLastUpdateTime() {
    return lastUpdateTime;
  }

  public String getRenderContent() {
    return KmarkProcessor.process(content);
  }

  public String getEscapeContent() {
    return KmarkProcessor.escapeHtml(content);
  }

  public UUID getReplyToAccountId() {
    return replyToAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Debate debate = (Debate) o;

    if (!debateId.equals(debate.debateId)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return debateId.hashCode();
  }

  @Override
  public String toString() {
    return "Debate{" +
        "articleId=" + articleId +
        ", debateId=" + debateId +
        ", zone=" + zone +
        ", parentDebateId=" + parentDebateId +
        ", level=" + level +
        ", content='" + content + '\'' +
        ", contentType=" + contentType +
        ", debaterId=" + debaterId +
        ", debaterName='" + debaterName + '\'' +
        ", upVote=" + upVote +
        ", downVote=" + downVote +
        ", createTime=" + createTime +
        ", lastUpdateTime=" + lastUpdateTime +
        ", replyToAccountId=" + replyToAccountId +
        '}';
  }

  public long getTotalVote() {
    return upVote - downVote;
  }

  public boolean isMaxLevel() {
    return level >= MAX_LEVEL;
  }

  public boolean isParent(Debate child) {
    return parentDebateId.equals(child.debateId);
  }

  public boolean canEdit(Authorization auth) {
    return auth.belongToAccount(debaterId);
  }

  public Debate withVote(long newUpVote, long newDownVote) {
    return new Debate(articleId,
        debateId,
        zone,
        parentDebateId,
        replyToAccountId,
        level,
        content,
        contentType,
        debaterId,
        debaterName,
        newUpVote,
        newDownVote,
        createTime,
        lastUpdateTime);
  }

  public V1DebateDto toV1Dto() {
    return new V1DebateDto(articleId.toString(),
        debateId.toString(),
        zone.value(),
        hasParent() ? parentDebateId.toString() : null,
        level,
        content,
        debaterName,
        upVote,
        downVote,
        Date.from(createTime),
        Date.from(lastUpdateTime));
  }

  public Zone getZone() {
    return zone;
  }

  public boolean isEdited() {
    return lastUpdateTime.isAfter(createTime);
  }

  public String getShortUrlPath() {
    return String.format("/d/%s", getDebateId());
  }
}