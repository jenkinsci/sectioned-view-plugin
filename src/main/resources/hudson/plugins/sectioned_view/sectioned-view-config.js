Behaviour.specify(".sectioned-view-tree__button", "sectioned-view__button", 0, function(btn) {
  btn.addEventListener("click", function(evt) {
    const container = btn.closest(".sectioned-view-tree__container");
    if (container) {
      const labelList = container.querySelector(".sectioned-view-tree");
      if (labelList) {
        labelList.classList.toggle("jenkins-hidden");
        if (btn.dataset.hidden === "true") {
          btn.dataset.hidden = "false";
        } else {
          btn.dataset.hidden = "true";
        }
      }
    }
  });
});