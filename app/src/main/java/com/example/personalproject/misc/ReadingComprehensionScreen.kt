package app.kotori.japanese.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

// ── Data model ────────────────────────────────────────────────────────────────

private data class ReadingQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
)

private data class ReadingPassage(
    val title: String,
    val level: String,
    val text: String,
    val questions: List<ReadingQuestion>,
)

// ── Passage data — JLPT-style comprehension texts ─────────────────────────────

private val passages: List<ReadingPassage> = listOf(

    // ────────── N5 ──────────────────────────────────────────────────────────

    ReadingPassage(
        title = "田中さんの一日",
        level = "N5",
        text = """
田中さんは毎朝七時に起きます。朝ごはんは、ごはんとたまごです。
八時に家を出て、電車で会社に行きます。会社は九時から五時までです。
夜は友だちとよく映画を見ます。田中さんは映画がとても好きです。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "田中さんは何時に起きますか。",
                options = listOf("六時", "七時", "八時", "九時"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "田中さんは会社に何で行きますか。",
                options = listOf("バスで", "車で", "電車で", "自転車で"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "田中さんが好きなものはなんですか。",
                options = listOf("音楽", "スポーツ", "りょうり", "映画"),
                correctIndex = 3,
            ),
        ),
    ),

    ReadingPassage(
        title = "お店のお知らせ",
        level = "N5",
        text = """
スーパーやまもと
　　　　お知らせ

今週の土曜日と日曜日は、野菜が半額です。
牛肉も安いです。ぜひ来てください。

営業時間：午前九時〜午後八時
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "いつ野菜が安いですか。",
                options = listOf("月曜日と火曜日", "水曜日と木曜日", "金曜日", "土曜日と日曜日"),
                correctIndex = 3,
            ),
            ReadingQuestion(
                question = "このお店は何時に閉まりますか。",
                options = listOf("午後六時", "午後七時", "午後八時", "午後九時"),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "天気予報",
        level = "N5",
        text = """
あした、東京は雨です。気温は十五度です。
かさを持って出かけてください。
週末は晴れる予定です。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "あしたの東京の天気はどうですか。",
                options = listOf("晴れ", "くもり", "雨", "雪"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "あしたの気温は何度ですか。",
                options = listOf("五度", "十度", "十五度", "二十度"),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N4 ──────────────────────────────────────────────────────────

    ReadingPassage(
        title = "図書館のルール",
        level = "N4",
        text = """
市立図書館　ご利用のルール

・本は最大５冊まで借りられます。
・貸出期間は２週間です。延長は１回だけできます。
・館内での飲食は禁止です。
・携帯電話は図書館の外でご使用ください。
・本を返す場合は、正面玄関の返却ボックスをご利用ください。開館時間外も使えます。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "一度に何冊まで本を借りることができますか。",
                options = listOf("２冊", "３冊", "５冊", "１０冊"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "貸出期間の延長について、正しい説明はどれですか。",
                options = listOf("延長できない", "何度でも延長できる", "１回だけ延長できる", "２週間延長できる"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "閉館後に本を返したい場合は、どうすればいいですか。",
                options = listOf("翌日に来る", "正面玄関の返却ボックスを使う", "スタッフに電話する", "ポストに入れる"),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "友人へのメール",
        level = "N4",
        text = """
件名：来週の予定について

ゆきさん、

お元気ですか。先週のパーティー、楽しかったですね！

来週の土曜日、一緒に映画を見に行きませんか。見たい映画があって、午後２時から新宿の映画館でやっています。映画の後、近くのレストランで夕ごはんを食べましょう。

都合が悪ければ、日曜日でも大丈夫ですよ。

返事をください。
さくらより
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "さくらさんはゆきさんをどこに誘っていますか。",
                options = listOf("パーティー", "コンサート", "映画", "レストランだけ"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "映画は何時から始まりますか。",
                options = listOf("午前２時", "午後２時", "午後４時", "夕方"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "もし土曜日が都合が悪ければ、どうすればいいですか。",
                options = listOf("映画をやめる", "別の映画館に行く", "日曜日にする", "さくらさんが一人で行く"),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N3 ──────────────────────────────────────────────────────────

    ReadingPassage(
        title = "SNSと孤独",
        level = "N3",
        text = """
現代の若者はSNSで多くの人とつながっているが、実際には孤独を感じている人が増えているという調査結果が発表された。
SNSでは「いいね」の数や友達の数が多いほど人気があるように見えるが、画面の向こうの関係は表面的なものになりがちだ。
専門家は「大切なのは量より質だ」と指摘する。少数でも深い人間関係を築くことが、精神的な健康につながると述べている。
SNSを使う時間を減らし、直接会って話す機会を増やすことが、孤独感の解消につながるかもしれない。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "調査によると、現代の若者はどのような状況ですか。",
                options = listOf("SNSをほとんど使わない", "SNSでつながりながらも孤独を感じている", "友達がとても多い", "インターネットが嫌いだ"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "専門家が大切だと述べているのはどれですか。",
                options = listOf("SNSの友達の数を増やすこと", "「いいね」の数を気にすること", "少数でも深い人間関係を築くこと", "新しいSNSを始めること"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "この文章の内容と合うものはどれですか。",
                options = listOf(
                    "SNSを使えば孤独は感じない",
                    "直接会う機会を増やすことが孤独感の解消につながるかもしれない",
                    "「いいね」が多い人ほど幸せだ",
                    "現代の若者は孤独を感じていない",
                ),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "公共マナーについて",
        level = "N3",
        text = """
最近、電車や公共の場所でのマナーが問題になっている。特に、スマートフォンを見ながら歩く「ながらスマホ」は危険だとして、各地で条例を設ける自治体が増えている。

一方で、マナーは強制するものではなく、個人の意識の問題だという意見もある。ルールを増やすよりも、教育を通じて社会全体のマナー意識を高めることが重要だという考え方だ。

どちらの立場も、公共の場での他者への配慮が必要だという点では一致している。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "「ながらスマホ」とは何ですか。",
                options = listOf(
                    "スマホで音楽を聴くこと",
                    "歩きながらスマホを見ること",
                    "電車の中でスマホを充電すること",
                    "スマホでゲームをすること",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "マナーについて、異なる立場が一致している点はどれですか。",
                options = listOf(
                    "ルールを増やすべきだ",
                    "教育が最も大切だ",
                    "公共の場で他者への配慮が必要だ",
                    "スマホの使用を禁止すべきだ",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N2 ──────────────────────────────────────────────────────────

    ReadingPassage(
        title = "働き方改革の課題",
        level = "N2",
        text = """
日本政府は「働き方改革」を推進し、長時間労働の是正や有給休暇の取得促進などを図ってきた。しかし、法律が整備されても、実態が変わらない職場も多い。

その背景には、上司より先に帰りにくいという日本独自の職場文化や、残業を頑張りの証とみなす価値観がある。制度を変えるだけでは不十分で、意識改革が不可欠だという指摘は多い。

また、リモートワークの普及により、仕事と私生活の境界が曖昧になる新たな問題も生じている。「オンとオフ」を切り替えられず、結果的に労働時間が増えてしまうケースも報告されている。

働き方を本当に変えるには、制度・文化・テクノロジーの三つの側面から包括的に取り組む必要があると専門家は強調する。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "働き方改革が実態として進みにくい理由として挙げられているのはどれですか。",
                options = listOf(
                    "政府が改革に反対しているから",
                    "上司より先に帰りにくい文化や残業を美徳とする価値観があるから",
                    "リモートワークが普及していないから",
                    "有給休暇の日数が少ないから",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "リモートワークの普及により生じた新たな問題はどれですか。",
                options = listOf(
                    "通勤時間が増えた",
                    "職場のコミュニケーションが改善された",
                    "仕事と私生活の境界が曖昧になり、労働時間が増えるケースがある",
                    "有給休暇が取りやすくなった",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "専門家が強調する働き方改革に必要な取り組みはどれですか。",
                options = listOf(
                    "制度の整備だけで十分である",
                    "テクノロジーの導入のみが解決策だ",
                    "文化的な変革だけが重要だ",
                    "制度・文化・テクノロジーの三つの側面からの包括的な取り組みが必要だ",
                ),
                correctIndex = 3,
            ),
        ),
    ),

    ReadingPassage(
        title = "情報リテラシーの重要性",
        level = "N2",
        text = """
インターネット上には膨大な情報が存在するが、その中には誤った情報やデマも少なくない。SNSでは、感情的に共感しやすい内容が拡散されやすく、事実確認がされないまま広まってしまうことがある。

こうした状況を受け、「情報リテラシー」の教育が注目されている。情報リテラシーとは、情報を批判的に読み解き、その信頼性を判断する能力のことだ。

例えば、ある情報を目にしたとき、「誰が発信しているか」「根拠は示されているか」「複数の情報源で確認できるか」といった点を検討することが重要とされる。

デジタル社会において、情報リテラシーはもはや特別なスキルではなく、日常生活に必要な基本的な能力と位置付けられつつある。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "SNSで誤情報が拡散しやすい理由はどれですか。",
                options = listOf(
                    "SNSには情報を確認するシステムがあるから",
                    "感情的に共感しやすい内容が拡散されやすいから",
                    "情報リテラシー教育が普及しているから",
                    "インターネットの速度が遅いから",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "情報リテラシーとはどのような能力ですか。",
                options = listOf(
                    "情報を素早くインターネットで検索する能力",
                    "SNSで情報を拡散する能力",
                    "情報を批判的に読み解き、その信頼性を判断する能力",
                    "プログラミングを使って情報を処理する能力",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "文章の内容と合うものはどれですか。",
                options = listOf(
                    "情報リテラシーは専門家だけに必要なスキルだ",
                    "インターネット上の情報はすべて正確である",
                    "デジタル社会では情報リテラシーが日常に必要な能力になりつつある",
                    "複数の情報源で確認する必要はない",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N1 ──────────────────────────────────────────────────────────

    ReadingPassage(
        title = "言語と思考の関係",
        level = "N1",
        text = """
人間の思考は言語によって形作られるのか、それとも言語から独立して存在するのか。この問いは哲学や言語学において長年議論されてきた。

サピア＝ウォーフ仮説によれば、人が使う言語の構造が思考様式に影響を与えるとされる。例えば、色の区別を細かく表現できる言語を使う人々は、その区別をより鋭敏に認識するという研究結果がある。しかし、この仮説に対しては批判も多く、言語が思考を完全に規定するわけではないという立場も根強い。

一方、認知言語学の観点からは、概念の形成において言語と思考が相互に影響し合うという見方もある。言語は単なる表現の道具ではなく、世界の捉え方そのものを構成する要素だという考え方だ。

この問題に対する決定的な答えはまだ出ていないが、異なる言語を習得することで思考の幅が広がるという経験的な証言は多く、言語と思考の関係が無関係でないことは確かだといえよう。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "サピア＝ウォーフ仮説の主な主張はどれですか。",
                options = listOf(
                    "思考は言語から完全に独立している",
                    "使用する言語の構造が思考様式に影響を与える",
                    "すべての言語は同じように思考を形作る",
                    "言語は思考よりも重要ではない",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "認知言語学の観点から見た言語と思考の関係はどれですか。",
                options = listOf(
                    "言語は単なる表現の道具に過ぎない",
                    "思考が言語を決定する",
                    "言語と思考は互いに影響し合い、言語は世界の捉え方を構成する",
                    "言語と思考は無関係だ",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "この文章で筆者が示している結論はどれですか。",
                options = listOf(
                    "言語と思考は完全に無関係である",
                    "サピア＝ウォーフ仮説は完全に正しい",
                    "決定的な答えはないが、言語と思考は何らかの関係がある",
                    "異なる言語を学んでも思考は変わらない",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "「批判も多い」とあるが、批判の内容として本文から読み取れるものはどれですか。",
                options = listOf(
                    "言語が思考を完全に規定するわけではないという立場",
                    "色の区別の研究は正確ではないという立場",
                    "認知言語学は間違っているという立場",
                    "哲学と言語学は矛盾しているという立場",
                ),
                correctIndex = 0,
            ),
        ),
    ),

    ReadingPassage(
        title = "都市と地方の格差問題",
        level = "N1",
        text = """
日本における都市と地方の格差は、経済的・文化的側面においていまなお顕著である。東京をはじめとする大都市圏への人口集中は続いており、地方では過疎化と高齢化が深刻な課題となっている。

政府はこれまで「地方創生」を掲げてきたが、その成果は限定的との見方が多い。補助金や移住促進策は一定の効果をもたらすものの、根本的な産業構造の変革なしには持続的な発展は難しいという指摘がある。

デジタル技術の進歩により、場所を選ばずに働けるリモートワークが普及してきたことで、地方移住の障壁が下がりつつある。しかし、教育・医療・交通インフラの格差が解消されない限り、若い世代が地方に定着するには依然として高いハードルがある。

地方の課題解決には、中央主導ではなく地域が自律的に課題を設定し、住民参加型で進める「内発的発展」の視点が重要だと論じる研究者もいる。画一的な政策ではなく、地域の多様性を活かした柔軟なアプローチが求められている。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "地方創生の成果について、本文はどのような見方を示していますか。",
                options = listOf(
                    "大きな成功を収めている",
                    "限定的であり、根本的な産業構造の変革が必要だという指摘がある",
                    "リモートワークによって完全に解決された",
                    "補助金政策のみで十分だ",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "リモートワークの普及について、筆者が示している見解はどれですか。",
                options = listOf(
                    "地方移住の障壁を完全に取り除いた",
                    "地方移住の障壁を下げているが、インフラ格差などの課題が残る",
                    "若者の地方定着には関係がない",
                    "教育・医療の問題を解決した",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "「内発的発展」とはどのような考え方ですか。",
                options = listOf(
                    "中央政府が主導して地方に政策を押し付ける考え方",
                    "外国からの投資を促進する考え方",
                    "地域が自律的に課題を設定し、住民参加型で進める考え方",
                    "都市から地方への人口移動を強制する考え方",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "本文の主旨として最も適切なものはどれですか。",
                options = listOf(
                    "東京への一極集中はもはや止められない",
                    "地方創生は補助金政策を続ければ解決する",
                    "都市・地方の格差解消には、インフラ整備・産業構造改革・地域主体のアプローチが必要だ",
                    "リモートワークだけが地方活性化の答えだ",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N5 (additional) ──────────────────────────────────────────────

    ReadingPassage(
        title = "やまださんのしゅみ",
        level = "N5",
        text = """
やまださんのしゅみは写真をとることです。
毎週末、こうえんや山に行きます。
カメラはちちにもらいました。
やまださんはいぬやねこの写真がとくにすきです。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "やまださんのしゅみはなんですか。",
                options = listOf("りょうり", "えをかくこと", "写真をとること", "おんがく"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "やまださんはどこに行きますか。",
                options = listOf("うみやかわ", "こうえんや山", "みせやデパート", "としょかん"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "カメラはだれにもらいましたか。",
                options = listOf("はは", "ちち", "ともだち", "せんせい"),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "かいものリスト",
        level = "N5",
        text = """
きょう、スーパーで買うもの：

・たまご　　　　一パック
・パン　　　　　二つ
・ぎゅうにゅう　一本
・りんご　　　　三つ
・とりにく　　　四百グラム

ぜんぶで二千円ぐらいです。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "りんごはいくつ買いますか。",
                options = listOf("一つ", "二つ", "三つ", "四つ"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "ぎゅうにゅうはいくつ買いますか。",
                options = listOf("一パック", "一本", "二本", "三つ"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "ぜんぶでいくらぐらいですか。",
                options = listOf("千円", "千五百円", "二千円", "三千円"),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "学校のおしらせ",
        level = "N5",
        text = """
学校のおしらせ

らいしゅうの月曜日はおやすみです。
火曜日から学校があります。
かばんにえんぴつとノートをいれてください。
しゅくだいは水曜日までです。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "らいしゅうの月曜日はどうですか。",
                options = listOf("学校がある", "おやすみ", "しゅくだいがある", "テストがある"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "しゅくだいはいつまでですか。",
                options = listOf("月曜日", "火曜日", "水曜日", "木曜日"),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "ともだちへのてがみ",
        level = "N5",
        text = """
みきへ

こんにちは。げんきですか？

わたしはせんしゅう、かぞくとうみに行きました。
とてもたのしかったです。
うみでおよいで、すなはまでごはんをたべました。

また一緒にあそびましょう。

ゆきより
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "ゆきさんはせんしゅう、どこに行きましたか。",
                options = listOf("やま", "こうえん", "うみ", "としょかん"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "だれと行きましたか。",
                options = listOf("ともだち", "かぞく", "せんせい", "ひとり"),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "うみでなにをしましたか。",
                options = listOf("しゃしんをとった", "ほんをよんだ", "およいだ", "ねた"),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N4 (additional) ──────────────────────────────────────────────

    ReadingPassage(
        title = "健康のためのアドバイス",
        level = "N4",
        text = """
毎日の生活に少し運動を取り入れるだけで、健康状態が大きく変わります。
特別なジムに通わなくても、通勤や買い物のときに歩く距離を増やすことができます。

また、食事も大切です。野菜や魚を多く食べ、脂っこい食べ物はなるべく控えましょう。
水をたくさん飲むことも体にいいといわれています。

さらに、十分な睡眠を取ることも健康には欠かせません。
毎晩同じ時間に寝る習慣をつけると、体のリズムが整います。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "この文章によると、運動はどうすればよいですか。",
                options = listOf(
                    "毎日ジムに通わなければならない",
                    "特別なジムに通う必要がある",
                    "通勤や買い物のときに歩く距離を増やすことができる",
                    "運動は健康に関係がない",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "食事について、何を控えるべきですか。",
                options = listOf("野菜", "魚", "水", "脂っこい食べ物"),
                correctIndex = 3,
            ),
            ReadingQuestion(
                question = "睡眠のためにどんな習慣がいいですか。",
                options = listOf(
                    "毎日違う時間に寝る",
                    "毎晩同じ時間に寝る習慣をつける",
                    "運動の後すぐに寝る",
                    "昼に長く寝る",
                ),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "アルバイトの求人",
        level = "N4",
        text = """
【アルバイト募集】

カフェ「さくら」では、スタッフを募集しています。

■仕事内容：接客、コーヒーの準備、簡単な調理補助
■時給：1,050円
■勤務時間：週3日以上、一日4時間以上
■応募条件：18歳以上、経験不問
■交通費：全額支給

ご興味のある方は、店長の田中まで電話かメールでご連絡ください。
電話：03-XXXX-XXXX
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "このアルバイトの時給はいくらですか。",
                options = listOf("900円", "1,000円", "1,050円", "1,200円"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "応募するためにどんな条件が必要ですか。",
                options = listOf(
                    "25歳以上であること",
                    "経験が必要",
                    "週5日働けること",
                    "18歳以上であること",
                ),
                correctIndex = 3,
            ),
            ReadingQuestion(
                question = "応募するにはどうすればいいですか。",
                options = listOf(
                    "直接店に行く",
                    "田中店長に電話かメールで連絡する",
                    "インターネットで申し込む",
                    "友達に紹介してもらう",
                ),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "旅行の計画メール",
        level = "N4",
        text = """
件名：夏休みの旅行について

ひろしくんへ

来月の夏休みに、一緒に京都に行きませんか。
京都には古いお寺や神社がたくさんあって、とても有名な観光地です。

電車で東京から約２時間半で着きます。
ホテルは一泊８千円くらいのところを考えています。
２泊３日の予定で行きたいと思っています。

もしよかったら、来週の金曜日までに返事をください。

けんじより
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "けんじさんはどこに旅行したいですか。",
                options = listOf("大阪", "東京", "京都", "奈良"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "東京から京都まで電車でどのくらいかかりますか。",
                options = listOf("約１時間", "約２時間", "約２時間半", "約３時間"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "旅行は何日間の予定ですか。",
                options = listOf("１泊２日", "２泊３日", "３泊４日", "１日だけ"),
                correctIndex = 1,
            ),
        ),
    ),

    // ────────── N3 (additional) ──────────────────────────────────────────────

    ReadingPassage(
        title = "読書の習慣",
        level = "N3",
        text = """
近年、スマートフォンやインターネットの普及により、本を読む人が減ってきているといわれている。
しかし、読書には多くのメリットがある。

まず、語彙力や読解力が向上する。文章を読むことで、知らない言葉の意味を文脈から推測する練習ができる。
次に、集中力が鍛えられる。長い文章を最後まで読むためには、一定の集中力が必要だからだ。

また、読書はストレス解消にも効果的だという研究結果もある。
フィクションを読むことで、現実から少し離れてリラックスできるという。

忙しい現代人でも、毎日少しの時間を読書にあてることで、これらの効果を得ることができる。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "なぜ近年、本を読む人が減ってきているといわれていますか。",
                options = listOf(
                    "本が高くなったから",
                    "スマートフォンやインターネットが普及したから",
                    "図書館が減ったから",
                    "本の内容が難しくなったから",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "読書が集中力を鍛えるといわれる理由はどれですか。",
                options = listOf(
                    "短い文章を読むから",
                    "長い文章を最後まで読むために一定の集中力が必要だから",
                    "難しい言葉が多いから",
                    "読書は静かな場所でするものだから",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "この文章が最も伝えたいことはどれですか。",
                options = listOf(
                    "スマートフォンは使わないほうがいい",
                    "読書には語彙力・集中力向上やストレス解消など多くのメリットがある",
                    "フィクションだけを読むべきだ",
                    "毎日２時間以上読書しなければならない",
                ),
                correctIndex = 1,
            ),
        ),
    ),

    ReadingPassage(
        title = "環境問題とわたしたちの生活",
        level = "N3",
        text = """
地球温暖化や海洋汚染など、環境問題が深刻になっている。
その原因の一つは、私たちの日常生活にある。

例えば、プラスチック製品の使い捨ては、海や土の汚染につながる。
また、自動車や工場から出る二酸化炭素が温暖化を進める原因とされている。

しかし、個人でもできることはある。
買い物袋を持参する、公共交通機関を使う、電気をこまめに消すなど、小さな行動でも積み重ねれば大きな効果につながる。

環境を守るためには、政府や企業の取り組みだけでなく、一人ひとりの意識が大切だ。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "環境問題の原因として、本文で挙げられているものはどれですか。",
                options = listOf(
                    "人口の増加",
                    "プラスチックの使い捨てや二酸化炭素の排出",
                    "都市化の進行",
                    "農業の拡大",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "個人でできる環境保護の行動として挙げられていないものはどれですか。",
                options = listOf("買い物袋を持参する", "公共交通機関を使う", "電気をこまめに消す", "新しい車を買う"),
                correctIndex = 3,
            ),
            ReadingQuestion(
                question = "筆者は環境問題の解決に何が必要だと述べていますか。",
                options = listOf(
                    "政府の取り組みだけで十分だ",
                    "企業が全責任を負うべきだ",
                    "政府・企業の取り組みに加え、一人ひとりの意識が大切だ",
                    "技術の進歩だけが解決策だ",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "外国語学習のコツ",
        level = "N3",
        text = """
外国語を上達させるために、いくつかの効果的な方法がある。

まず、毎日少しずつでも勉強を続けることが大切だ。まとめて勉強するよりも、短時間でも毎日続けるほうが記憶に残りやすい。

次に、実際に使う機会を作ることが重要だ。外国人の友達を作ったり、語学交換パートナーを探したりすることで、本物のコミュニケーションを経験できる。

また、映画やドラマ、音楽などで楽しみながら学ぶのも効果的だ。楽しい気持ちで学ぶと、より記憶に残りやすい。

完璧を目指しすぎて間違いを恐れると、なかなか上達しない。
間違えながらでも積極的に使っていくことが上達の近道だ。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "外国語学習について、まとめて勉強することと毎日続けることの比較として正しいものはどれですか。",
                options = listOf(
                    "まとめて勉強するほうが記憶に残りやすい",
                    "短時間でも毎日続けるほうが記憶に残りやすい",
                    "どちらも同じ効果がある",
                    "まとめて勉強するほうが楽しい",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "「楽しみながら学ぶ」方法の例として挙げられているものはどれですか。",
                options = listOf("文法書を読む", "テストを受ける", "映画やドラマ、音楽で学ぶ", "単語カードを作る"),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "筆者が「上達の近道」として挙げているのはどれですか。",
                options = listOf(
                    "完璧な文章だけを話すこと",
                    "間違えないようにゆっくり話すこと",
                    "間違えながらでも積極的に使っていくこと",
                    "まず文法を完全にマスターすること",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    // ────────── N2 (additional) ──────────────────────────────────────────────

    ReadingPassage(
        title = "人工知能と雇用の未来",
        level = "N2",
        text = """
人工知能（AI）技術の急速な発展により、多くの職業が自動化される可能性が指摘されている。
特にルーティンワークや単純作業の分野では、AIが人間に代わる場面が増えつつある。

しかし、この変化を悲観的にのみとらえるのは早計だという意見もある。
歴史的に見ると、技術革新はいつも新たな職業を生み出してきた。
蒸気機関の登場で農業従事者が減少した一方、工場労働者という職業が生まれたように、AIも同様の変化をもたらす可能性がある。

重要なのは、変化に対応できるよう人材育成を進めることだ。
論理的思考力や創造性、コミュニケーション能力など、AIが苦手とする能力を伸ばすことが求められる。
また、生涯学習の文化を社会全体で育て、個人が状況の変化に柔軟に対応できる環境を整備することも必要だ。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "AIの発展により特に影響を受けやすいとされる仕事はどれですか。",
                options = listOf(
                    "創造性を必要とする仕事",
                    "ルーティンワークや単純作業",
                    "コミュニケーションを主とする仕事",
                    "AIの開発に関わる仕事",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "歴史的な例として蒸気機関が挙げられているのはなぜですか。",
                options = listOf(
                    "蒸気機関は失業者を増やしたから",
                    "技術革新が新たな職業を生み出してきたことを示すため",
                    "工場労働者がなくなることを示すため",
                    "農業が重要だったことを示すため",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "筆者がAI時代に必要だと述べているのはどれですか。",
                options = listOf(
                    "AIの使用を規制すること",
                    "すべての仕事をAIに任せること",
                    "論理的思考や創造性を育てる人材育成と生涯学習の文化",
                    "農業への回帰",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "消費者行動の変化",
        level = "N2",
        text = """
インターネットの普及と共に、消費者の購買行動は大きく変化した。
かつては店舗に足を運び、商品を実際に確認してから購入するのが一般的だったが、今では自宅にいながらスマートフォン一つで何でも購入できる。

この変化により、実店舗は苦境に立たされているが、一方で新たな役割を担いつつある。
体験型の展示や対面でのカウンセリングなど、オンラインでは提供しにくいサービスに特化する戦略をとる店舗が増えている。

また、消費者の価値観も変化している。価格だけでなく、企業の環境への取り組みや社会貢献活動を重視して購買先を選ぶ「エシカル消費」の考え方が広まっている。

このように、市場環境の変化の中で企業は単に商品を売るだけでなく、消費者との関係性を構築することが重要になってきている。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "インターネットの普及後、消費者の購買行動はどう変わりましたか。",
                options = listOf(
                    "店舗での購入がさらに増えた",
                    "自宅でスマートフォン一つで購入できるようになった",
                    "価格を重視しなくなった",
                    "海外製品を買わなくなった",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "実店舗の新たな役割として挙げられているものはどれですか。",
                options = listOf(
                    "低価格競争でオンラインに対抗すること",
                    "体験型の展示や対面カウンセリングに特化すること",
                    "商品の種類を減らすこと",
                    "配送サービスを充実させること",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "「エシカル消費」とはどのような考え方ですか。",
                options = listOf(
                    "最も安い商品を選ぶ考え方",
                    "有名ブランドを優先する考え方",
                    "企業の環境への取り組みや社会貢献を重視して購買先を選ぶ考え方",
                    "国産品だけを購入する考え方",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "少子化問題の背景",
        level = "N2",
        text = """
日本の少子化は深刻な社会問題となっている。出生率の低下は、将来的な労働力不足や社会保障制度の持続可能性に大きな影響を与える。

少子化の要因は複合的だ。晩婚化・未婚化の進行、子育てにかかる経済的負担の大きさ、長時間労働による育児との両立の難しさ、そして女性のキャリア形成に対する社会的な制約などが挙げられる。

政府はこれまで様々な対策を打ってきたが、根本的な改善には至っていない。保育所の拡充や育児休業制度の整備は進んでいるものの、職場の意識改革や社会全体の価値観の転換がなければ、制度だけでは限界があるという指摘は多い。

少子化問題を解決するためには、子育てを社会全体で支える文化を育てることが不可欠だと多くの専門家は強調している。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "少子化が問題とされる理由として本文で挙げられていないものはどれですか。",
                options = listOf(
                    "将来的な労働力不足",
                    "社会保障制度への影響",
                    "都市部への人口集中",
                    "社会の持続可能性への影響",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "少子化の要因として挙げられていないものはどれですか。",
                options = listOf(
                    "晩婚化・未婚化の進行",
                    "子育ての経済的負担",
                    "外国人労働者の増加",
                    "長時間労働による育児との両立の難しさ",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "政府の対策について筆者はどう述べていますか。",
                options = listOf(
                    "保育所の拡充など制度整備が十分に効果を上げている",
                    "制度整備は進んでいるが、意識・価値観の変革がなければ限界があると指摘されている",
                    "政府は何も対策を取っていない",
                    "制度を増やすだけで問題は解決できる",
                ),
                correctIndex = 1,
            ),
        ),
    ),

    // ────────── N1 (additional) ──────────────────────────────────────────────

    ReadingPassage(
        title = "日本語の曖昧さと文化",
        level = "N1",
        text = """
日本語には、文脈に依存した曖昧な表現が多いとされる。主語や目的語が省略されることが多く、また「はい」「いいえ」の使い方も、欧米語と単純に対応するわけではない。

このような特徴は、しばしば「日本語は曖昧だ」という批判の対象となってきた。しかし、これを単純に欠点ととらえるのは適切ではないだろう。曖昧さは、場の空気を読み、相手との関係を維持しながらコミュニケーションを行う文化的文脈の中で機能しているからだ。

言語学者のエドワード・ホールが提唱したハイコンテクスト文化とローコンテクスト文化という概念がある。ハイコンテクスト文化では、言語そのものよりも文脈・状況・関係性が重要な意味を持つ。日本はこの典型例とされる。

異文化コミュニケーションの場において、こうした言語の背後にある文化的前提を理解することは、誤解を防ぐうえで不可欠だ。言葉を額面通りに受け取るだけでなく、その背景にある意図や文脈を読み解く能力が、グローバル化社会では特に求められている。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "日本語の「曖昧さ」について、筆者の立場はどれですか。",
                options = listOf(
                    "曖昧さは欠点であり改善すべきだ",
                    "曖昧さは文化的文脈の中で機能しており、単純に欠点とはいえない",
                    "曖昧さは日本語の学習を妨げる",
                    "曖昧さは現代には不要だ",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "ハイコンテクスト文化の説明として正しいものはどれですか。",
                options = listOf(
                    "言語が唯一の意味伝達手段である文化",
                    "文法規則が非常に厳格な文化",
                    "言語よりも文脈・状況・関係性が重要な意味を持つ文化",
                    "多言語を使用する文化",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "グローバル化社会で特に求められると筆者が述べている能力はどれですか。",
                options = listOf(
                    "多くの外国語を習得すること",
                    "言葉の背後にある意図や文脈を読み解く能力",
                    "曖昧な表現を使わないこと",
                    "ハイコンテクスト文化を避けること",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "エドワード・ホールの概念が引用されている目的はどれですか。",
                options = listOf(
                    "日本語の文法の複雑さを説明するため",
                    "言語学者の権威を示すため",
                    "日本語の曖昧さが文化的背景を持つことを理論的に裏付けるため",
                    "欧米語の優位性を示すため",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "自然と人間の共存",
        level = "N1",
        text = """
産業革命以降、人間は自然を支配・利用の対象としてとらえてきた。資源の採掘、森林の伐採、化石燃料の大量消費は、急速な経済発展をもたらす一方、生態系の破壊や気候変動といった深刻な問題を引き起こした。

環境倫理学の分野では、このような人間中心主義的な自然観に疑問が呈されてきた。人間が自然に対して持つべきは支配者としての立場ではなく、同じ生態系の一員としての謙虚さではないかという主張がある。

また、生物多様性の保全という観点からも、人間の経済活動が生態系に及ぼす影響は軽視できない。ある種の絶滅は、食物連鎖や生態系サービスのバランスを崩し、最終的には人間自身の生存基盤を脅かす可能性がある。

持続可能な社会の実現には、経済的発展と環境保全を対立として捉えるのではなく、両者を統合する新たなパラダイムが求められている。
技術革新のみならず、自然との関係を根本的に問い直す哲学的・文化的な変革もまた必要不可欠だと論じる声は多い。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "産業革命以降の人間の自然に対する姿勢はどのようなものですか。",
                options = listOf(
                    "自然を保護の対象としてきた",
                    "自然を支配・利用の対象としてきた",
                    "自然と平等な関係を築いてきた",
                    "自然を崇拝の対象としてきた",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "環境倫理学が問題としている「人間中心主義」とはどのような考え方ですか。",
                options = listOf(
                    "人間が自然の一部であるという考え方",
                    "自然界のすべての生物は平等だという考え方",
                    "人間が自然に対して支配者の立場をとる考え方",
                    "人間は自然の脅威から身を守るべきという考え方",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "ある種の絶滅が人間に影響を与える理由として正しいものはどれですか。",
                options = listOf(
                    "美しい景観が失われるから",
                    "食物連鎖や生態系サービスのバランスが崩れ、人間の生存基盤が脅かされるから",
                    "その生物から得られる薬が失われるから",
                    "観光資源がなくなるから",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "筆者が持続可能な社会の実現に必要だと述べているものはどれですか。",
                options = listOf(
                    "経済発展と環境保全のどちらかを選択すること",
                    "技術革新だけに頼ること",
                    "経済発展と環境保全を統合する新パラダイムと哲学的・文化的な変革",
                    "産業革命以前の生活に戻ること",
                ),
                correctIndex = 2,
            ),
        ),
    ),

    ReadingPassage(
        title = "民主主義とポピュリズム",
        level = "N1",
        text = """
近年、世界各地で既成の政治エリートや専門家層への不信感が高まり、ポピュリズムと呼ばれる政治運動が台頭している。ポピュリズムとは一般に、「腐敗したエリート」対「純粋な民衆」という二項対立を強調し、民衆の声を直接代弁すると主張する政治スタイルを指す。

民主主義とポピュリズムの関係は複雑だ。一方では、長年無視されてきた人々の不満や要求を政治の場に引き出す機能を果たすこともある。しかし他方で、少数派の権利を軽視したり、複雑な政策問題を単純化しすぎたり、メディアや司法など民主主義を支える制度を攻撃したりする傾向も指摘される。

政治哲学者たちはこの問題に対して様々な見方を示している。ポピュリズムを民主主義の「影」として批判する立場もあれば、それを既存の代表制民主主義が抱える正当性の危機への応答として読み解く立場もある。

いずれにせよ、民主主義の健全性を保つためには、市民が多様な情報源から批判的に情報を収集し、複雑な問題に単純な答えを求めすぎないリテラシーを持つことが、かつてないほど重要になっている。
        """.trimIndent(),
        questions = listOf(
            ReadingQuestion(
                question = "ポピュリズムの特徴として本文が挙げているものはどれですか。",
                options = listOf(
                    "エリートと民衆が協力することを強調する",
                    "「腐敗したエリート」対「純粋な民衆」という二項対立を強調する",
                    "少数派の権利を積極的に守る",
                    "専門家の意見を重視する",
                ),
                correctIndex = 1,
            ),
            ReadingQuestion(
                question = "ポピュリズムの肯定的な側面として挙げられているものはどれですか。",
                options = listOf(
                    "政策を複雑にして詳細に議論する",
                    "メディアや司法を強化する",
                    "長年無視されてきた人々の不満や要求を政治の場に引き出す",
                    "少数派の権利を守る",
                ),
                correctIndex = 2,
            ),
            ReadingQuestion(
                question = "ポピュリズムの問題点として挙げられていないものはどれですか。",
                options = listOf(
                    "少数派の権利の軽視",
                    "複雑な政策問題の単純化",
                    "民主主義を支える制度への攻撃",
                    "国際的な連帯の促進",
                ),
                correctIndex = 3,
            ),
            ReadingQuestion(
                question = "筆者が民主主義の健全性を保つために重要だと述べているのはどれですか。",
                options = listOf(
                    "ポピュリズム運動を法律で禁止すること",
                    "政治エリートに政策決定を任せること",
                    "市民が批判的に情報を収集し、単純な答えを求めすぎないリテラシーを持つこと",
                    "多数派の意見だけを政治に反映させること",
                ),
                correctIndex = 2,
            ),
        ),
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

private val levels = listOf("N5", "N4", "N3", "N2", "N1")

@Composable
fun ReadingComprehensionScreen(onBack: () -> Unit) {
    var selectedLevelIndex by remember { mutableIntStateOf(0) }
    var activePassage by remember { mutableStateOf<ReadingPassage?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Reading Comprehension",
            description = "Practice reading long-form passages at N2–N1 difficulty.\n\n" +
                "• Select a difficulty tab (N3 / N2 / N1) to see available passages.\n" +
                "• Tap a passage to read it, then answer comprehension questions.\n" +
                "• Read the full text carefully before answering — questions test both detail and overall understanding.\n" +
                "• After answering all questions, see your score and review correct/incorrect answers.\n\n" +
                "This section targets the Dokkai (reading comprehension) portion of the JLPT exam.",
            onDismiss = { showHelp = false },
        )
    }

    if (activePassage != null) {
        PassageQuiz(
            passage = activePassage!!,
            onBack = { activePassage = null },
        )
        return
    }

    val selectedLevel = levels[selectedLevelIndex]
    val levelPassages = passages.filter { it.level == selectedLevel }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Reading Comprehension",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            },
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Level tabs
            ScrollableTabRow(
                selectedTabIndex = selectedLevelIndex,
                edgePadding = 16.dp,
                divider = {},
            ) {
                levels.forEachIndexed { index, lvl ->
                    Tab(
                        selected = selectedLevelIndex == index,
                        onClick = { selectedLevelIndex = index },
                        text = {
                            Text(
                                text = lvl,
                                fontWeight = if (selectedLevelIndex == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(4.dp))

            // Passage count + explanation
            Text(
                text = "${levelPassages.size} passages · read and answer questions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            levelPassages.forEach { passage ->
                PassageCard(passage = passage, onClick = { activePassage = passage })
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PassageCard(passage: ReadingPassage, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = passage.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${passage.questions.size} questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── Passage quiz ──────────────────────────────────────────────────────────────

@Composable
private fun PassageQuiz(passage: ReadingPassage, onBack: () -> Unit) {
    // Track selected answer per question index
    val selectedAnswers = remember { mutableStateListOf<Int?>().apply { repeat(passage.questions.size) { add(null) } } }
    var showResults by remember { mutableStateOf(false) }

    if (showResults) {
        PassageResults(passage = passage, selectedAnswers = selectedAnswers.toList(), onBack = onBack)
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = passage.title, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Passage text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp),
            ) {
                Text(
                    text = passage.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 26.sp,
                )
            }

            HorizontalDivider()

            Text(
                text = "Answer the questions:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            // Questions
            passage.questions.forEachIndexed { qIndex, question ->
                QuestionBlock(
                    number = qIndex + 1,
                    question = question,
                    selectedIndex = selectedAnswers[qIndex],
                    onSelect = { choiceIndex ->
                        if (selectedAnswers[qIndex] == null) {
                            selectedAnswers[qIndex] = choiceIndex
                        }
                    },
                )
            }

            // Show Results button (only when all questions answered)
            val allAnswered = selectedAnswers.none { it == null }
            if (allAnswered) {
                Button(
                    onClick = { showResults = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("See Results", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuestionBlock(
    number: Int,
    question: ReadingQuestion,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$number.　${question.question}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        question.options.forEachIndexed { idx, option ->
            val isSelected = selectedIndex == idx
            val isCorrect = idx == question.correctIndex
            val bgColor = when {
                selectedIndex == null -> MaterialTheme.colorScheme.surfaceVariant
                isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val borderColor = when {
                selectedIndex == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                isCorrect -> Color(0xFF2E7D32)
                isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = selectedIndex == null) { onSelect(idx) }
                    .padding(14.dp),
            ) {
                Text(
                    text = "${listOf("A", "B", "C", "D")[idx]}.  $option",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ── Results ───────────────────────────────────────────────────────────────────

@Composable
private fun PassageResults(
    passage: ReadingPassage,
    selectedAnswers: List<Int?>,
    onBack: () -> Unit,
) {
    val correct = passage.questions.indices.count { i ->
        selectedAnswers[i] == passage.questions[i].correctIndex
    }
    val total = passage.questions.size
    val pct = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0
    val passed = pct >= 60
    val emoji = when {
        pct >= 90 -> "🏆"
        pct >= 70 -> "🎉"
        pct >= 60 -> "👍"
        else -> "📚"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "${passage.title} — Results", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(emoji, fontSize = 56.sp)
            Text(
                text = "$correct / $total correct ($pct%)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )

            // Per-question review
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Answer review",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    passage.questions.forEachIndexed { i, q ->
                        val wasCorrect = selectedAnswers[i] == q.correctIndex
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (wasCorrect) Color(0xFF2E7D32)
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${i + 1}. ${q.question}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!wasCorrect) {
                                    Text(
                                        text = "Correct: ${q.options[q.correctIndex]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Back to passages", fontWeight = FontWeight.Bold)
            }
        }
    }
}
