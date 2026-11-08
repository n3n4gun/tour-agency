package ru.naboyshchikovfp.tour_agency.controllers;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;

import ru.naboyshchikovfp.tour_agency.dto.TourFilterDto;
import ru.naboyshchikovfp.tour_agency.dto.TourViewDto;
import ru.naboyshchikovfp.tour_agency.services.TourService;
import ru.naboyshchikovfp.tour_agency.services.ReviewService;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;

@Controller 
public class TourController {
    private final TourService tourService;
    private final ReviewService reviewService;

    public TourController(TourService tourService, ReviewService reviewService) {
        this.tourService = tourService;
        this.reviewService = reviewService;
    }

    // Показывает каталог или ошибки введённых фильтров.
    @GetMapping("/tours")
    public String showCatalog(
            @ModelAttribute("filter") TourFilterDto filter,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageNumber = Math.max(page, 0);

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id").descending());

        Page<TourViewDto> toursPage = Page.empty(pageable);

        // Не выполняем поиск, если параметры не удалось преобразовать в поля DTO.
        if (!bindingResult.hasErrors()) {
            try {
                toursPage = tourService.search(filter, pageable);
            } catch (ValidationException exception) {
                // Показываем бизнес-ошибку рядом с соответствующим фильтром.
                bindingResult.rejectValue(
                        exception.getField(),
                        "filter.error",
                        exception.getMessage()
                );
            }
        }

        model.addAttribute("toursPage", toursPage);

        return "tours";
    }

    @GetMapping("/tours/{tourId}")
    public String showTourDetails(@PathVariable("tourId") Long tourId, Model model, Principal principal) {
        TourViewDto tour = tourService.getDetails(tourId);

        model.addAttribute("tour", tour);
        model.addAttribute("reviews", reviewService.getByTour(tourId));

        boolean canUseClientActions = principal instanceof Authentication authentication
            && authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));

        model.addAttribute("canUseClientActions", canUseClientActions);
        model.addAttribute("isGuest", principal == null);

        return "tour-details";

    }

}
