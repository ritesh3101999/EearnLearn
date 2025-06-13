package com.earnlearn.controller;

import com.earnlearn.model.Course;
import com.earnlearn.model.Question;
import com.earnlearn.model.Quiz;
import com.earnlearn.service.CourseService;
import com.earnlearn.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses/{courseId}/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseService courseService;

    /**
     * Displays a list of quizzes for a specific course.
     * @param courseId The ID of the course.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @GetMapping
    public String listQuizzes(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        List<Quiz> quizzes = quizService.getQuizzesByCourse(courseId);
        model.addAttribute("course", course);
        model.addAttribute("quizzes", quizzes);
        return "quizzes/list";
    }

    /**
     * Shows the form to create a new quiz.
     * @param courseId The ID of the course.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @GetMapping("/new")
    public String showQuizForm(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        model.addAttribute("course", course);
        model.addAttribute("quiz", new Quiz());
        return "quizzes/form";
    }

    /**
     * Saves a new quiz or updates an existing one.
     * @param courseId The ID of the course.
     * @param quiz The Quiz object from the form.
     * @param redirectAttributes For flash messages.
     * @return Redirect URL.
     */
    @PostMapping("/save")
    public String saveQuiz(@PathVariable Long courseId, @ModelAttribute Quiz quiz, RedirectAttributes redirectAttributes) {
        quizService.saveQuiz(quiz, courseId);
        redirectAttributes.addFlashAttribute("successMessage", "Quiz saved successfully!");
        return "redirect:/courses/" + courseId + "/quizzes";
    }

    /**
     * Displays details of a specific quiz, including its questions.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @GetMapping("/{quizId}")
    public String viewQuiz(@PathVariable Long courseId, @PathVariable Long quizId, Model model) {
        Course course = courseService.getCourseById(courseId);
        Quiz quiz = quizService.getQuizById(quizId);
        List<Question> questions = quizService.getQuestionsByQuiz(quizId);
        model.addAttribute("course", course);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new Question()); // For adding new questions
        return "quizzes/detail";
    }

    /**
     * Deletes a quiz.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz to delete.
     * @param redirectAttributes For flash messages.
     * @return Redirect URL.
     */
    @PostMapping("/delete/{quizId}")
    public String deleteQuiz(@PathVariable Long courseId, @PathVariable Long quizId, RedirectAttributes redirectAttributes) {
        quizService.deleteQuiz(quizId);
        redirectAttributes.addFlashAttribute("successMessage", "Quiz deleted successfully!");
        return "redirect:/courses/" + courseId + "/quizzes";
    }

    /**
     * Shows the form to add or edit a question for a quiz.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param questionId Optional: The ID of the question to edit.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @GetMapping("/{quizId}/questions/form")
    public String showQuestionForm(@PathVariable Long courseId, @PathVariable Long quizId,
                                   @RequestParam(required = false) Long questionId, Model model) {
        Course course = courseService.getCourseById(courseId);
        Quiz quiz = quizService.getQuizById(quizId);
        Question question = (questionId != null) ? quizService.getQuestionById(questionId, quizId) : new Question();
        model.addAttribute("course", course);
        model.addAttribute("quiz", quiz);
        model.addAttribute("question", question);
        return "quizzes/question-form";
    }

    /**
     * Saves a new question or updates an existing one.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param question The Question object from the form.
     * @param optionsList List of options from the form (comma-separated string).
     * @param redirectAttributes For flash messages.
     * @return Redirect URL.
     */
    @PostMapping("/{quizId}/questions/save")
    public String saveQuestion(@PathVariable Long courseId, @PathVariable Long quizId,
                               @ModelAttribute Question question,
                               @RequestParam("optionsInput") String optionsList, // Get options as a single string
                               RedirectAttributes redirectAttributes) {
        // Parse the comma-separated options string into a List<String>
        List<String> options = List.of(optionsList.split(","))
                                   .stream()
                                   .map(String::trim)
                                   .filter(s -> !s.isEmpty())
                                   .collect(Collectors.toList());
        question.setOptions(options);

        quizService.saveQuestion(question, quizId);
        redirectAttributes.addFlashAttribute("successMessage", "Question saved successfully!");
        return "redirect:/courses/" + courseId + "/quizzes/" + quizId;
    }

    /**
     * Deletes a question.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param questionId The ID of the question to delete.
     * @param redirectAttributes For flash messages.
     * @return Redirect URL.
     */
    @PostMapping("/{quizId}/questions/delete/{questionId}")
    public String deleteQuestion(@PathVariable Long courseId, @PathVariable Long quizId,
                                 @PathVariable Long questionId, RedirectAttributes redirectAttributes) {
        quizService.deleteQuestion(questionId);
        redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully!");
        return "redirect:/courses/" + courseId + "/quizzes/" + quizId;
    }

    /**
     * Displays the quiz for the user to take.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @GetMapping("/{quizId}/take")
    public String takeQuiz(@PathVariable Long courseId, @PathVariable Long quizId, Model model) {
        Course course = courseService.getCourseById(courseId);
        Quiz quiz = quizService.getQuizById(quizId);
        List<Question> questions = quizService.getQuestionsByQuiz(quizId);
        model.addAttribute("course", course);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "quizzes/take";
    }

    /**
     * Submits the quiz answers and displays the result.
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param request HttpServletRequest to get submitted parameters.
     * @param model The model to add attributes to.
     * @return The Thymeleaf template name.
     */
    @PostMapping("/{quizId}/submit")
    public String submitQuiz(@PathVariable Long courseId, @PathVariable Long quizId,
                             HttpServletRequest request, Model model) {
        Map<Long, Integer> submittedAnswers = new HashMap<>();
        List<Question> questions = quizService.getQuestionsByQuiz(quizId);

        for (Question question : questions) {
            String paramName = "question_" + question.getId();
            String selectedOptionIndexStr = request.getParameter(paramName);
            if (selectedOptionIndexStr != null && !selectedOptionIndexStr.isEmpty()) {
                try {
                    submittedAnswers.put(question.getId(), Integer.parseInt(selectedOptionIndexStr));
                } catch (NumberFormatException e) {
                    // Handle invalid input if necessary
                }
            }
        }

        int score = quizService.evaluateQuiz(quizId, submittedAnswers);
        int totalQuestions = questions.size();

        model.addAttribute("course", courseService.getCourseById(courseId));
        model.addAttribute("quiz", quizService.getQuizById(quizId));
        model.addAttribute("score", score);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("submittedAnswers", submittedAnswers); // Pass answers for review
        model.addAttribute("questions", questions); // Pass questions for review

        return "quizzes/result";
    }
}