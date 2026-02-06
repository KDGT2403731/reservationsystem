package com.example.reservationsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.UserRepository;

@Controller
public class RegisterController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/register")
	public String registerForm() {
		return "register";
	}

	@PostMapping("/register")
	public String register(
			@RequestParam("name") String name,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("confirmPassword") String confirmPassword,
			@RequestParam("role") String role,
			Model model) {

		// バリデーション：役割の確認
		if (!role.matches("CUSTOMER|STAFF|ADMIN")) {
			model.addAttribute("error", "無効な役割が選択されています。");
			return "register";
		}

		// バリデーション：パスワード確認
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "パスワードと確認用パスワードが一致しません。");
			return "register";
		}

		// バリデーション：パスワード長
		if (password.length() < 6) {
			model.addAttribute("error", "パスワードは6文字以上で設定してください。");
			return "register";
		}

		// バリデーション：メールアドレスの重複確認
		if (userRepository.findByEmail(email).isPresent()) {
			model.addAttribute("error", "このメールアドレスは既に登録されています。");
			return "register";
		}

		// 新規ユーザーを作成
		User newUser = new User();
		newUser.setName(name);
		newUser.setEmail(email);
		newUser.setPassword(passwordEncoder.encode(password));
		newUser.setRole(role);

		userRepository.save(newUser);

		model.addAttribute("success", "登録が完了しました。ログインしてください。");
		return "register";
	}
}
