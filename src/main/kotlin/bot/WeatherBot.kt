package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import data.remote.WEATHER_API_KEY
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val IS_DAY = 1

private const val GIF_WAITING_URL =
    "https://media.giphy.com/media/QPQ3xlJhqR1BXl89RG/giphy.gif"
private const val BOT_TOKEN = "5179067943:AAFMNJmKT04hg8VdLk6M9CNNCkgfUSV0R7Q"
private const val TIMEOUT_TIME = 30

class WeatherBot(private val weatherRepository: WeatherRepository) {

    private lateinit var country: String
    private var _chatId: ChatId.Id? = null
    private val chatId by lazy { requireNotNull(_chatId) }

    fun createBot(): Bot {
        return bot {
            token = BOT_TOKEN
            timeout = TIMEOUT_TIME
            logLevel = LogLevel.Network.Body

            dispatch {
                setUpCommands()
                setUpCallbacks()
            }
        }
    }

    private fun Dispatcher.setUpCallbacks() {
        callbackQuery(callbackData = "getMyLocation") {
            bot.sendMessage(chatId = chatId, text = "Отправь мне свою локацию")
            location {
                CoroutineScope(Dispatchers.IO).launch {
                    val userCountryName = weatherRepository.getCountryNameByCoordinates(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString(),
                        format = "json"
                    ).address.state

                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Да, верно.",
                                callbackData = "yes_label"
                            )
                        )
                    )
                    country = userCountryName

                    bot.sendMessage(
                        chatId = chatId,
                        text = "Твой город - ${country}, верно? \n Если неверно, скинь локацию ещё раз",
                        replyMarkup = inlineKeyboardMarkup
                    )
                }
            }
        }

        callbackQuery(callbackData = "enterManually") {
            bot.sendMessage(chatId = chatId, text = "Хорошо, введите свой город.")
            message(Filter.Text) {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Верно.",
                            callbackData = "yes_label"
                        )
                    )
                )
                country = message.text.toString()
                bot.sendMessage(
                    chatId = chatId,
                    text = "Ваш город - ${message.text}, верно? \n Если неверно, введите свой город ещё раз.",
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }

        callbackQuery(callbackData = "yes_label") {
            bot.apply {
                sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
                sendMessage(chatId = chatId, text = "Минутку, узнаю вашу погоду...")
                sendChatAction(chatId = chatId, action = ChatAction.TYPING)
            }
            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    apiKey = WEATHER_API_KEY,
                    queryCountry = country,
                    isAqiNeeded = "no"
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = """
                            🌡 Температура (градусы): ${currentWeather.current.tempDegrees}
                            🙎 ‍Ощущается как: ${currentWeather.current.feelsLikeDegrees}
                            🌓 Сейчас день? ${if (currentWeather.current.isDay == IS_DAY) "Да" else "Нет"}
                        """.trimIndent()
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = "Если вы хотите запросить погоду ещё раз, \n воспользуйтесь командой /weather"
                )
                country = ""
            }
        }
    }

    private fun Dispatcher.setUpCommands() {
        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Привет! Я бот, умеющий показать погоду! \n Для запуска бота введи команду /weather"
            )
        }

        command("weather") {
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Введите ваш город",
                        callbackData = "enterManually"
                    )
                )
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Для того, чтобы я смог отправить вам погоду, \n мне нужно знать ваш город.",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
}